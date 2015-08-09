(function() {
    var canvas;
    var ctx;
    var history;
    var game;
    var boardIndex;
    var timer;
    var field;
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

    function drawBoard() {
        var width = game.settings.width;
        var height = game.settings.height;
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
                (field[r][c] == 1) ?  drawFullCell(drawPos.x, drawPos.y) : drawEmptyCell(drawPos.x, drawPos.y);
            }
        }
        game.diffBoards[boardIndex]['u'].forEach(function(e) {
            var drawPos = getDrawPosition(e);
            drawUnitCell(drawPos.x, drawPos.y);
        });
        {
            var drawPos = getDrawPosition(game.diffBoards[boardIndex]['p']);
            drawPivot(drawPos.x, drawPos.y);
        }
        $('#score').text(game.diffBoards[boardIndex]['s']);
    }

    function clearField() {
        field = [];
        for(var y = 0; y < game.settings.height; ++y) {
            field.push([]);
            for(var x = 0; x < game.settings.width; ++x) {
                field[y].push(0);
            }
        }
        game.initialBoard.fullCells.forEach(function(pos) {
            field[pos.y][pos.x] = 1;
        });
    }

    function simulate(to) {
        var start = (boardIndex <= to) ? boardIndex+1 : 0;
        if(start == 0) {
            clearField();
        }
        for(var i = start; i <= to; ++i) {
            // Added
            game.diffBoards[i]['a'].forEach(function(pos) {
                field[pos.y][pos.x] = 1;
            });
            // Deleted
            game.diffBoards[i]['d'].forEach(function(pos) {
                field[pos.y][pos.x] = 0;
            });
        }
    }

    function setBoardIndex(index) {
        if(index < 0 || index >= game.diffBoards.length) {
            return false;
        }
        simulate(index);
        boardIndex = index;
        $('#current-state').val(index);
        drawBoard();
        return true;
    }

    function parseParameter() {
        var paramStr = window.location.search.substr(1);
        if(paramStr === null) return false;
        var params = paramStr.split('&');
        var revision, seed, problemId;
        params.forEach(function(param) {
            console.log(param);
            var arr = param.split('=');
            if(arr[0] == 'revision') {
                revision = arr[1];
            } else if(arr[0] == 'seed') {
                seed = arr[1];
            } else if(arr[0] == 'problemId') {
                problemId = arr[1];
            }
        });
        $.ajax({
            url: '/akatsuki/game/' + problemId + '/' + seed + '/' + revision + '/',
            contentType: 'application/json',
            method: 'GET'
        }).done(function(obj) {
            game = obj;
            $('#max').text(game.diffBoards.length - 1);
            setBoardIndex(0);
            drawBoard();
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
                        drawBoard();
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

        parseParameter();
    });
})();
