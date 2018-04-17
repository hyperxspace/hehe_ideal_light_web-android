const Data = require('./data');
const Util = require('./util');
const express = require('express');
const app = express();
const bodyParser = require('body-parser');
app.use(bodyParser.json({ limit: '1mb' }));
app.use(bodyParser.urlencoded({ extended: false }));
// Data.initData();
const data = Data.loadData();

function find(title, type) {
    let d;
    if (type == 0) {
        d = data.one;
    } else {
        d = data.more;
    }
    if (d[title] == null) {
        let max = 0;
        let a;
        for (t in d) {
            let s = Util.similarity(title, t);
            if (s > max) {
                max = s;
                a = d[t];
            }
        }
        return a.answer;
    } else {
        return d[title].answer;
    }
}

app.post('/', function(req, res) {
    const title = req.body.title;
    const type = req.body.type;
    if (title != null && type != null) {
        const result = find(title, type);
        res.json({
            result: result
        });
    } else {
        res.json({
            error: '必须要有参数 title 和 type'
        });
    }
    res.end();
});

app.listen(9090, '127.0.0.1');
