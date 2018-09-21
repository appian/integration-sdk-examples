const CLIENT_API_FRIENDLY_NAME = "TextDetectionClientApi";
const TEXT_FOUND = 'textFound';

var connectedSystem, imageUrl, canvas, ctx;

// Event callbacks
$(document).ready(function () {
    canvas = $('#canvas')[0];
    ctx = canvas.getContext("2d");

    $('#detect').on("click", detect);

    Appian.Component.onNewValue('imageUrl', url => {
        imageUrl = url;
        loadImage();
    });

    Appian.Component.onNewValue('connectedSystem', cs => {
        connectedSystem = cs;
    });
});

// Utility methods
function loadImage() {
    $('#detect').prop("disabled", true);
    $('#canvas').css('backgroundImage', `url(${imageUrl})`);
    canvasMaintenance();
}

function clearCanvas() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
}

function canvasMaintenance() {
    // Clear the strokes on the canvas
    clearCanvas();

    // Resize the canvas to match the image
    var img = new Image();
    img.addEventListener("load", function () {
        canvas.width = this.naturalWidth;
        canvas.height = this.naturalHeight;
        $('#detect').prop("disabled", false);
    });
    img.src = imageUrl;
}

// Invoke and Handle ClientApi response
function detect() {
    if (!connectedSystem || !imageUrl) {
        return;
    }

    const payload = { imageUrl: imageUrl };

    Appian.Component.invokeClientApi(connectedSystem, CLIENT_API_FRIENDLY_NAME, payload)
        .then(handleClientApiResponse)
        .catch(handleError);
}

function handleError(response) {
    if (response.error && response.error[0]) {
        Appian.Component.setValidations([error.error]);
    } else {
        Appian.Component.setValidations(["An unspecified error occurred"]);
    }
}

function handleClientApiResponse(response) {
    // Clear any error messages
    Appian.Component.setValidations([]);
    var outputResponse = response.payload.outputResponse;
    Appian.Component.saveValue(TEXT_FOUND, outputResponse.textFound);

    // Clear the strokes on the canvas
    clearCanvas();

    ctx.lineWidth = 3;

    // Draw the bounding boxes around the text found
    outputResponse.boundingBoxes.forEach(
        boundingBox => ctx.strokeRect(
            boundingBox.x,
            boundingBox.y,
            boundingBox.width,
            boundingBox.height
        )
    );
}