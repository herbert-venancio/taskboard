function clone(obj) {
    return jQuery.extend(true, {}, obj);
}

function sortByProperty(property) {
    return function(a,b) { return a[property] - b[property] };
}

function findInArray(array, callback) {
    var notFoundValue = undefined;
    var arrayLength = array.length;
    for (var i = 0; i < arrayLength; i++)
        if(callback(array[i]))
            return array[i];
    return notFoundValue;
}

function findIndexInArray(array, callback) {
    var notFoundValue = -1;
    var arrayLength = array.length;
    for (var i = 0; i < arrayLength; i++)
        if(callback(array[i]))
            return i;
    return notFoundValue;
}

if (!Object.values) {
    Object.values = function(obj) {
        return Object.keys(obj).map(
            function(key) { return obj[key] }
        );
    };
}

if (!Array.prototype.findIndex) {
    // https://developer.mozilla.org/pt-BR/docs/Web/JavaScript/Reference/Global_Objects/Array/findIndex#Polyfill
    Array.prototype.findIndex = function(predicate) {
        if (this === null) {
            throw new TypeError('Array.prototype.findIndex called on null or undefined');
        }
        if (typeof predicate !== 'function') {
            throw new TypeError('predicate must be a function');
        }
        var list = Object(this);
        var length = list.length >>> 0;
        var thisArg = arguments[1];
        var value;

        for (var i = 0; i < length; i++) {
            value = list[i];
            if (predicate.call(thisArg, value, i, list)) {
                return i;
            }
        }
        return -1;
    };
}