const greeter = require('./greeter');
const greeting = greeter.greetings("Vert.x");
const el = document.createElement('h1');
el.innerHTML = greeting;
document.body.appendChild(el);
