var express = require('express')
var app = express()
var pg = require('pg')
var formidable = require('express-formidable')
var http = require('http')
var https = require('https')
var imgur = require('imgur')
var unirest = require("unirest")
var fs = require('fs')
var braintree = require("braintree")

app.use(formidable.parse())

var gateway = braintree.connect({
  environment: braintree.Environment.Sandbox,
  merchantId: "******",
  publicKey: "******",
  privateKey: "******"
})

//Home Page?
app.get('/', function (req, res) {
	res.send('Say hello to Eden!')
})

//Check Database Transactions
app.get('/db', function (req, res) {
	pg.connect(process.env.DATABASE_URL, function(err, client, done) {
		client.query('SELECT * FROM users', function(err, result) {
			done()
			if(err) {
				console.error(err)
				res.json('Error' + err)
			} else {
				res.json({results: result.rows})
			}
		})
	})
})

//Email Endpoint that Takes Base64 Images via the email headers
app.post('/mail-endpoint', function(req, res) {
	var request_params = req.body.subject.split('-')
	var endpoint = request_params[0]
	var content = req.body.text
	var now = Date.now()

	console.log('Calling: ' + endpoint)

	if(endpoint == 'mood') {
		var userid = request_params[1]
		var base64Data = content.replace(/^data:image\/jpeg;base64,/, "")

		imgur.setClientId('******')
		imgur.uploadBase64(base64Data)
		.then(function (json) {
			var request = unirest("POST", "https://api.projectoxford.ai/emotion/v1.0/recognize");

			request.headers({
			  "postman-token": "******",
			  "cache-control": "no-cache",
			  "content-type": "application/json",
			  "ocp-apim-subscription-key": "******"
			});

			request.send("{ url: '"+ json.data.link +"' }")

			request.end(function (response) {
			  if (response.error) {
			  	throw new Error(response.error)
			  	res.json('Failed')
			  } else {
			  	if(response.body.length > 0) {
			  		var metrics = response.body[0]['scores']
				  	pg.connect(process.env.DATABASE_URL, function(error, client, done) {
						client.query('INSERT INTO mood (user_id, anger, contempt, disgust, fear, happiness, neutral, sadness, surprise) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9) RETURNING id',
										[userid,metrics.anger,metrics.contempt,metrics.disgust,metrics.fear,metrics.happiness,metrics.neutral,metrics.sadness,metrics.surprise], function(err, result) {
							done()
							if(error) {
								console.error(error)
							}
						})
					})
				  	res.json('Success')
				}
			  }
			})
	    })
	    .catch(function (err) {
	        console.error(err.message)
	        res.json(err.message)
	    })
	} else {
		res.json('Error')
	}
})

//Get the last processed mood via the email webhook
// - If we had integrated Pusher we could have possibly avoided this seperate function
app.get('/get-lastmood', function(req, res) {
	pg.connect(process.env.DATABASE_URL, function(err, client, done) {
		client.query('SELECT * FROM mood WHERE user_id = $1 ORDER BY id LIMIT 1', [6], function(err, result) {
			done()
			if(err) {
				console.error(err)
				res.json('Error' + err)
			} else {
				res.json({result})
			}
		})
	})
})

//Get X amount of mood points to graph in the app
app.get('/get-moods', function(req, res) {
	pg.connect(process.env.DATABASE_URL, function(err, client, done) {
		client.query('SELECT * FROM mood WHERE user_id = $1 ORDER BY id LIMIT $2', [6, req.query.count], function(err, result) {
			done()
			if(err) {
				console.error(err)
				res.json('Error' + err)
			} else {
				res.json({result})
			}
		})
	})
})

//Gets user's overall average for a specified mood
app.get('/average', function(req, res) {
	var mood_type = req.query.mood_type
	pg.connect(process.env.DATABASE_URL, function(err, client, done) {
		client.query('SELECT AVERAGE($2) FROM mood WHERE user_id = $1', [6, mood_type], function(err, result) {
			done()
			if(err) {
				console.error(err)
				res.json('Error' + err)
			} else {
				res.json({result})
			}
		})
	})
})

//Create New User along with a Paypal Customer Account
app.post('/users', function(req, res) {
	var query = "INSERT INTO users (username, f_name, l_name) VALUES ($1, $2, $3) RETURNING id"
	var params = [req.query.username, req.query.f_name, req.query.l_name]
	gateway.customer.create({
	  firstName: req.query.f_name,
	  lastName: req.query.l_name
	}, function (err, result) {
	  if(result.success) {
	  	query = "INSERT INTO users (username, f_name, l_name, id) VALUES ($1, $2, $3, $4) RETURNING id"
	  	params.push(results.cusotmer.id)
	  }
	})

	pg.connect(process.env.DATABASE_URL, function(err, client, done) {
		client.query(query, params, function(err, result) {
			done()
			if(err) {
				console.error(err)
				res.json('Error' + err)
			} else {
				res.json({
					id: result.rows[0].id
				})
			}
		})
	})
})

//Default client_token that we can use
app.get("/client_token", function (req, res) {
  gateway.clientToken.generate({}, function (err, response) {
    console.log(response.clientToken)
    res.send(response.clientToken)
  })
})

//Create the first transaction that will add the customer to vaulted payments
app.post("/checkout", function (req, res) {
  var nonce = req.body.payment_method_nonce;
  gateway.transaction.sale({
	  amount: '10.00',
	  paymentMethodNonce: nonce,
	  customer: {
	    id: '6'
	  },
	  options: {
	    storeInVaultOnSuccess: true
	  }
	}, function (err, result) {
		console.log('Failed: ' + err)
	})
})

//Listenning to active port on Heroku or PORT 5000 when on localhost
app.listen(process.env.PORT||5000)