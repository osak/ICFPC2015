(function() {
    var canvas;
    var ctx;
    var history;
    var boardIndex;
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

    function getDrawPosition(pos) {
        var offset = (pos.y % 2 == 0) ? 0 : HEX_WIDTH / 2;
        return {
            x: offset + ORIGIN.x + pos.x * HEX_WIDTH,
            y: ORIGIN.y + pos.y * HEX_HEIGHT
        };
    }

    function drawBoard(board) {
        var boardWidth = board.width * HEX_WIDTH + HEX_SIZE*3;
        var boardHeight = board.height * HEX_WIDTH + HEX_SIZE*3;
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
        for(var r = 0; r < board.height; ++r) {
            for(var c = 0; c < board.width; ++c) {
                var drawPos = getDrawPosition({y: r, x: c});
                drawEmptyCell(drawPos.x, drawPos.y);
            }
        }
        board.fullCells.forEach(function(e) {
            var drawPos = getDrawPosition(e);
            drawFullCell(drawPos.x, drawPos.y);
        });
        board.unitCells.forEach(function(e) {
            var drawPos = getDrawPosition(e);
            drawUnitCell(drawPos.x, drawPos.y);
        });
    }

    function initProblemSelector() {
        // Setup problem list
        var selector = $('#problem-selector');
        for(var i = 0; i <= 23; ++i) {
            selector.append($('<option value="' + i + '" name="problem-id">problem_' + i + '.json</option>'));
        }

        selector.change(function() {
            var id = $('#problem-selector').val();
            $.ajax({
                url: '/sakimori/problems/problem_' + id + '.json',
                contentType: 'text/json'
            }).done(function(obj) {
                drawBoard({
                    height: obj.height,
                    width: obj.width,
                    fullCells: obj.filled
                });
            });
        });
    }

    function setBoardIndex(index) {
        if(index < 0 || index > history.length) {
            return false;
        }
        boardIndex = index;
        $('#current-state').val(index);
        drawBoard(history[boardIndex]);
    }

    $(document).ready(function() {
        canvas = $('#canvas').get(0);
        ctx = canvas.getContext('2d');

        // Setup simulator action
        $('#simulator').click(function() {
            var raw = $('#simulator-out').val();
            eval('history = ' + raw);
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

        initProblemSelector();
    });
})();
