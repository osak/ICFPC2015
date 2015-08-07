#define _CRT_SECURE_NO_WARNINGS


#include <cstdio>
#include <vector>
#include <string>
#include <queue>
#include <set>
#include <map>
#include <algorithm>

using namespace std;

class Board {
    public:
    
    int currentScore;
    int previousLine;
    int expectedScore;
    string commands;
    vector <vector <int> > field;
    
    bool operator<(const Board &b) const {
        return currentScore + expectedScore < b.currentScore + b.expectedScore;
    }
};

class Point {
    public:
    
    int x;
    int y;
    
    bool operator<(const Point &p) const {
        if (x != p.x) return x < p.x;
        return y < p.y;
    }
};

class Unit {
    public:
    
    Point pivot;
    vector <Point> member;
};

const int beamWidth = 1;
int H, W;
int pxx[6] = {1, 1, 0, -1, -1, 0};
int pxy[6] = {0, 1, 1, 0, -1, -1};
int pyx[6] = {0, -1, -1, 0, 1, 1};
int pyy[6] = {1, 0, -1, -1, 0, 1};
int dx[4] = {0, 0, 1, 1};
int dy[2][4] = {1, -1, 0, -1, 1, -1, 1, 0};
int rdx[4] = {0, 0, -1, -1};
int rdy[2][4] = {-1, 1, -1, 0, -1, 1, 0, 1};
string commandMove[4] = {"E", "W", "ES", "WS"};
string commandRotate[3] = {"WCC", "", "WC"};
vector <Unit> units;
vector <int> source;

int calc(vector <vector <int> > &field, int num) {
    // TODO:盤面から評価値を計算
    return 0;
}

void init(Unit &unit) {
    int topx = 1e9, lefty = 1e9, righty = 1e9, i;
    
    for (i = 0; i < unit.member.size(); i++) {
        if (unit.pivot.x % 2 == 0) {
            unit.member[i].y = unit.member[i].y - unit.pivot.y - (unit.member[i].x - unit.pivot.x - 1) / 2;
        } else {
            unit.member[i].y = unit.member[i].y - unit.pivot.y - (unit.member[i].x - unit.pivot.x + 1) / 2;
        }
        
        topx = min(topx, unit.member[i].x);
        unit.member[i].x -= unit.pivot.x;
    }
    
    unit.pivot.x -= topx;
    
    for (i = 0; i < unit.member.size(); i++) {
        int y = unit.pivot.y + unit.member[i].y + (unit.pivot.x + unit.member[i].x) / 2;
        
        lefty = min(lefty, y);
        righty = min(righty, W - y - 1);
    }
    
    if (lefty >= righty) {
        unit.pivot.y -= (lefty - righty + 1) / 2;
    } else {
        unit.pivot.y += (righty - lefty) / 2;
    }
}

Point get(Point &pivot, int theta, Point &point) {
    int px, py;
    Point p;
    
    px = pxx[theta] * point.x + pxy[theta] * point.y;
    py = pyx[theta] * point.x + pyy[theta] * point.y;
    
    p.x = pivot.x + px;
    if (pivot.x % 2 == 0) {
        if (px > 0) {
            p.y = pivot.y + py + px / 2;
        } else {
            p.y = pivot.y + py + (px - 1) / 2;
        }
    } else {
        if (px > 0) {
            p.y = pivot.y + py + (px + 1) / 2;
        } else {
            p.y = pivot.y + py + px / 2;
        }
    }
    
    return p;
}

bool check(Board &board, Point &point, int theta, int num) {
    int i;
    
    for (i = 0; i < units[source[num]].member.size(); i++) {
        Point p = get(point, theta, units[source[num]].member[i]);
        if (p.x < 0 || p.x >= H || p.y < 0 || p.y >= W || board.field[p.x][p.y] == 1) return false;
    }
    
    return true;
}

void update(Board &board, Point &pivot, int theta, int num) {
    int count = 0, point, i, j;
    
    for (i = 0; i < units[source[num]].member.size(); i++) {
        Point p = get(pivot, theta, units[source[num]].member[i]);
        
        board.field[p.x][p.y] = 1;
    }
    
    for (i = H - 1; i >= 0; i--) {
        for (j = 0; j < W; j++) {
            if (board.field[i][j] == 0) break;
        }
        
        if (j == W) {
            count++;
        } else {
            board.field[i + count] = board.field[i];
        }
    }
    
    for (i = 0; i < count; i++) {
        for (j = 0; j < W; j++) {
            board.field[i][j] = 0;
        }
    }
    
    point = units[source[num]].member.size() + 100 * (1 + count) * count / 2;
    board.currentScore += point;
    if (board.previousLine > 1) board.currentScore += (board.previousLine - 1) * point / 10;
    board.previousLine = count;
}

void inputFromFile(string s){
	freopen(s.c_str(), "r", stdin);
}
void outputToFile(string s){
	freopen(s.c_str(), "w", stdout);
}

int main()
{
	//inputFromFile("../cpp_input/problem_0_0.txt");
	//outputToFile("out.txt");

	int unitCount, fieldCount, sourceLength, maxScore = -1, i, j, k;
    string ans = "";
    Board initBoard;
    priority_queue <Board> que, queNext;
    
    scanf("%d %d", &H, &W);
    scanf("%d", &unitCount);
    
    for (i = 0; i < unitCount; i++) {
        int count;
        Unit unit;
        
        scanf("%d %d %d", &unit.pivot.y, &unit.pivot.x, &count);
        
        for (j = 0; j < count; j++) {
            Point point;
            
            scanf("%d %d", &point.y, &point.x);
            
            unit.member.push_back(point);
        }
        
        init(unit);
        
        units.push_back(unit);
    }
    
    vector <vector <int> > field(H, vector <int>(W, 0));
    
    scanf("%d", &fieldCount);
    
    for (i = 0; i < fieldCount; i++) {
        int x, y;
        
        scanf("%d %d", &y, &x);
        
        field[x][y] = 1;
    }
    
    scanf("%d", &sourceLength);
    
    for (i = 0; i < sourceLength; i++) {
        int id;
        
        scanf("%d", &id);
        
        source.push_back(id);
    }
    
    initBoard.currentScore = initBoard.previousLine = 0;
    initBoard.expectedScore = calc(field, 0);
    initBoard.commands = "";
    initBoard.field = field;
    
    que.push(initBoard);
    
    for (i = 0; i < source.size(); i++) {
        for (j = 0; j < beamWidth && !que.empty(); j++) {
            Board board = que.top();
            queue <pair<Point, int> > queBFS;
            set <Board> states;
            map <pair<Point, int>, int> parent;
            
            que.pop();
            
            if (board.currentScore > maxScore) {
                maxScore = board.currentScore;
                ans = board.commands;
            }
            
            parent[make_pair(units[source[i]].pivot, 0)] = -1;
            queBFS.push(make_pair(units[source[i]].pivot, 0));
            
            while (!queBFS.empty()) {
                bool insert = false;
                Point point = queBFS.front().first;
                int theta = queBFS.front().second;
                
                queBFS.pop();
                
                // 移動
                for (k = 0; k < 4; k++) {
                    Point nextPoint = point;
                    
                    nextPoint.x += dx[k];
                    nextPoint.y += dy[point.x % 2][k];
                    
                    if (check(board, nextPoint, theta, i)) {
                        if (!parent.count(make_pair(nextPoint, theta))) {
                            parent[make_pair(nextPoint, theta)] = k;
                            queBFS.push(make_pair(nextPoint, theta));
                        }
                    } else if (!insert) {
                        insert = true;
                        Board nextBoard = board;
                        
                        update(nextBoard, point, theta, i);
                        
                        if (states.count(nextBoard)) continue;
                        states.insert(nextBoard);
                        
                        Point nowPoint = point;
                        int nowTheta = theta;
                        string commands = "";
                        while (1) {
                            int commandNum = parent[make_pair(nowPoint, nowTheta)];
                            
                            if (commandNum == -1) break;
                            
                            if (commandNum <= 3) {
                                commands += commandMove[commandNum];
                                Point newPoint = nowPoint;
                                newPoint.x += rdx[commandNum];
                                newPoint.y += rdy[nowPoint.x % 2][commandNum];
                                nowPoint = newPoint;
                            } else {
                                commandNum -= 5;
                                commands += commandRotate[commandNum + 1];
                                nowTheta = (nowTheta - commandNum + 6) % 6;
                            }
                        }
                        reverse(commands.begin(), commands.end());
                        nextBoard.commands += commands;
                        nextBoard.expectedScore = calc(nextBoard.field, i + 1);
                        queNext.push(nextBoard);
                    }
                }
                
                // 回転
                for (k = -1; k <= 1; k++) {
                    if (k == 0) continue;
                    
                    int nextTheta = (theta + k + 6) % 6;
                    
                    if (check(board, point, nextTheta, i)) {
                        if (!parent.count(make_pair(point, nextTheta))) {
                            parent[make_pair(point, nextTheta)] = k + 5;
                            queBFS.push(make_pair(point, nextTheta));
                        }
                    } else if (!insert) {
                        insert = true;
                        Board nextBoard = board;
                        
                        update(nextBoard, point, theta, i);
                        
                        if (states.count(nextBoard)) continue;
                        states.insert(nextBoard);
                        
                        Point nowPoint = point;
                        int nowTheta = theta;
                        string commands = "";
                        while (1) {
                            int commandNum = parent[make_pair(nowPoint, nowTheta)];
                            
                            if (commandNum == -1) break;
                            
                            if (commandNum <= 3) {
                                commands += commandMove[commandNum];
                                Point newPoint = nowPoint;
                                newPoint.x += rdx[commandNum];
                                newPoint.y += rdy[nowPoint.x % 2][commandNum];
                                nowPoint = newPoint;
                            } else {
                                commandNum -= 5;
                                commands += commandRotate[commandNum + 1];
                                nowTheta = (nowTheta - commandNum + 6) % 6;
                            }
                        }
                        reverse(commands.begin(), commands.end());
                        nextBoard.commands += commands;
                        nextBoard.expectedScore = calc(nextBoard.field, i + 1);
                        queNext.push(nextBoard);
                    }
                }
            }
        }
        
        while (!que.empty()) {
            if (que.top().currentScore > maxScore) {
                maxScore = que.top().currentScore;
                ans = que.top().commands;
            }
            
            que.pop();
        }
        
        swap(que, queNext);
    }
    
    if (!que.empty() && que.top().currentScore > maxScore) ans = que.top().commands;
    
    printf("%s\n", ans.c_str());
    
    return 0;
}
