const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const staticDir = path.join(__dirname, 'src', 'static');
const icons = [
    'home.png', 'home-active.png',
    'cart.png', 'cart-active.png',
    'order.png', 'order-active.png',
    'user.png', 'user-active.png'
];

async function compressIcons() {
    for (const icon of icons) {
        const inputPath = path.join(staticDir, icon);
        const tempPath = path.join(staticDir, 'temp_' + icon);

        if (fs.existsSync(inputPath)) {
            // Resize to 81x81 and compress
            await sharp(inputPath)
                .resize(81, 81, { fit: 'contain', background: { r: 0, g: 0, b: 0, alpha: 0 } })
                .png({ compressionLevel: 9 })
                .toFile(tempPath);

            const stats = fs.statSync(tempPath);
            console.log(`${icon}: ${(stats.size / 1024).toFixed(2)} KB`);
        }
    }

    // Now replace originals
    for (const icon of icons) {
        const inputPath = path.join(staticDir, icon);
        const tempPath = path.join(staticDir, 'temp_' + icon);
        if (fs.existsSync(tempPath)) {
            fs.rmSync(inputPath);
            fs.renameSync(tempPath, inputPath);
        }
    }
}

compressIcons().then(() => console.log('Done!')).catch(console.error);
