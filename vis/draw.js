(function() {
    var canvas;
    var ctx;
    var history;
    var boardIndex;
    var timer;
    var settings;
    var HEX_SIZE = 20;
    var HEX_WIDTH = HEX_SIZE * Math.sqrt(3);
    var HEX_HEIGHT = HEX_SIZE * 1.5;
    var ORIGIN = {x: 20, y: 20};
    var CANVAS_ORIGINAL_DIM = {width: 800, height: 500};

    function setLineColor(color) {
        ctx.strokeStyle = color;
    }

    function setFillColor(color) {
        ctx.fillStyle = color;
    }

    // Draw hex centered at (x, y)
    function drawHex(x, y) {
        var start = Math.PI / 6;
        ctx.beginPath();
        ctx.moveTo(x + Math.cos(start) * HEX_SIZE, y + Math.sin(start) * HEX_SIZE);
        for(var i = 0; i < 6; ++i) {
            var angle = start + 2 * Math.PI * i / 6;
            ctx.lineTo(x + Math.cos(angle + 2 * Math.PI / 6) * HEX_SIZE, y + Math.sin(angle + 2 * Math.PI / 6) * HEX_SIZE);
        }
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    function drawEmptyCell(x, y) {
        setLineColor('black');
        setFillColor('white');
        drawHex(x, y);
    }

    function drawFullCell(x, y) {
        setLineColor('black');
        setFillColor('yellow');
        drawHex(x, y);
    }

    function drawUnitCell(x, y) {
        setLineColor('black');
        setFillColor('red');
        drawHex(x, y);
    }

    function drawPivot(x, y) {
        setLineColor('gray');
        setFillColor('gray');
        ctx.beginPath();
        ctx.arc(x, y, HEX_SIZE / 4, 0, 2 * Math.PI, true);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    function getDrawPosition(pos) {
        var offset = (pos.y % 2 == 0) ? 0 : HEX_WIDTH / 2;
        return {
            x: offset + ORIGIN.x + pos.x * HEX_WIDTH,
            y: ORIGIN.y + pos.y * HEX_HEIGHT
        };
    }

    function drawBoard(board) {
        var width = settings ? settings.width : board.width;
        var height = settings ? settings.height : board.height;
        var boardWidth = width * HEX_WIDTH + HEX_SIZE*3;
        var boardHeight = height * HEX_WIDTH + HEX_SIZE*3;
        if($('#scale').prop('checked')) {
            canvas.width = CANVAS_ORIGINAL_DIM.width;
            canvas.height = CANVAS_ORIGINAL_DIM.height;
            var scale = Math.min(1, Math.min(canvas.width / boardWidth, canvas.height / boardHeight)) ;
            ctx.scale(scale, scale);
        } else {
            canvas.width = boardWidth;
            canvas.height = boardHeight;
            ctx.scale(1, 1);
        }
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        for(var r = 0; r < height; ++r) {
            for(var c = 0; c < width; ++c) {
                var drawPos = getDrawPosition({y: r, x: c});
                drawEmptyCell(drawPos.x, drawPos.y);
            }
        }
        board.fullCells.forEach(function(e) {
            var drawPos = getDrawPosition(e);
            drawFullCell(drawPos.x, drawPos.y);
        });
        if(board.unitCells) {
            board.unitCells.forEach(function(e) {
                var drawPos = getDrawPosition(e);
                drawUnitCell(drawPos.x, drawPos.y);
            });
        }
        if(board.pivot) {
            var drawPos = getDrawPosition(board.pivot);
            drawPivot(drawPos.x, drawPos.y);
        }
        $('#score').text(board.score);
    }

    function initProblemSelector() {
        // Setup problem list
        var selector = $('#problem-selector');
        for(var i = 0; i <= 24; ++i) {
            selector.append($('<option value="' + i + '" name="problem-id">problem_' + i + '.json</option>'));
        }

        selector.change(function() {
            var id = $('#problem-selector').val();
            $.ajax({
                url: '/sakimori/problems/problem_' + id + '.json',
                contentType: 'text/json'
            }).done(function(obj) {
                init = {
                    height: obj.height,
                    width: obj.width,
                    fullCells: obj.filled
                };
                history = [];
                history.push(init);
                obj.units.forEach(function(e) {
                    var width = 0, height = 0;
                    e.members.forEach(function(m) {
                        width = Math.max(width, m.x+1);
                        height = Math.max(height, m.y+1);
                    });
                    history.push({
                        width: width,
                        height: height,
                        fullCells: e.members,
                        pivot: e.pivot
                    });
                });
                $('#max').text(history.length - 1);
                setBoardIndex(0);
            });
        });
    }

    function setBoardIndex(index) {
        if(index < 0 || index >= history.length) {
            return false;
        }
        boardIndex = index;
        $('#current-state').val(index);
        drawBoard(history[boardIndex]);
        return true;
    }

    var COMMAND_TABLE = {
        106: 'MOVE_W',
        107: 'MOVE_E',
        110: 'MOVE_SW',
        44:  'MOVE_SE',
        117: 'C_CLOCK',
        105: 'CLOCK'
    };

    function initGame() {
        $('#canvas').keypress(function(e) {
            var command = COMMAND_TABLE[e.charCode];
            var board = history[boardIndex];
            if(command) {
                $.ajax({
                    url: 'http://icfpc.osak.jp/miichan',
                    contentType: 'application/json',
                    method: 'POST',
                    data: JSON.stringify({
                        command: command,
                        board: board
                    })
                }).done(function(res) {
                    console.log(res);
                    history[boardIndex] = res.Board;
                    drawBoard(history[boardIndex]);
                });
            }
        });
    }

    $(document).ready(function() {
        canvas = $('#canvas').get(0);
        ctx = canvas.getContext('2d');

        // Setup simulator action
        $('#simulator').click(function() {
            settings = undefined;
            var raw = $('#simulator-out').val();
            eval('json  = ' + raw);
            settings = json.settings;
            history = json.boards;
            $('#max').text(history.length - 1);
            setBoardIndex(0);
        });

        $('#prev-button').click(function() {
            setBoardIndex(boardIndex - 1);
        });
        $('#next-button').click(function() {
            setBoardIndex(boardIndex + 1);
        });
        $('#current-state').focusout(function() {
            setBoardIndex(parseInt(this.value));
        });
        $('#current-state').keypress(function(e) {
            if(e.keyCode == 13) {
                setBoardIndex(parseInt(this.value));
            }
        });
        $('#auto-button').click(function() {
            if(timer) {
                clearInterval(timer);
                timer = undefined;
                $('#auto-button').text('Auto');
            } else {
                var wait = parseInt($('#auto-wait').val());
                timer = setInterval(function() {
                    if(setBoardIndex(boardIndex + 1)) {
                        drawBoard(history[boardIndex]);
                    } else {
                        clearInterval(timer);
                        timer = undefined;
                        $('#auto-button').text('Auto');
                    }
                }, wait);
                $('#auto-button').text('Stop');
            }
        });
        $('#dev-render').click(function() {
            var rev = $('#revision').val();
            var probid = parseInt($('#probid').val());
            var seed = parseInt($('#seed').val());
            var turn = parseInt($('#turn').val());
            var url;
            if(turn) {
                url = '/akatsuki/board/' + probid + '/' + seed + '/' + rev + '/' + turn + '/';
            } else {
                url = '/akatsuki/game/' + probid + '/' + seed + '/' + rev + '/';
            }

            $.ajax({
                url: url,
                contentType: 'application/json',
                method: 'GET'
            }).done(function(obj) {
                if(Array.isArray(obj)) {
                    history = [];
                    obj.forEach(function(o) {
                        history.push(o.board);
                    });
                } else {
                    history = [obj.board];
                }
                $('#max').text(history.length - 1);
                setBoardIndex(0);
            });
        });

        initProblemSelector();
        initGame();
    });
})();
