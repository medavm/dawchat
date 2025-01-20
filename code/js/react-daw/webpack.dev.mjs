export default {
	mode: 'development',
	devServer: {
		port: 5000,
		historyApiFallback: true,   //for react router
		compress: false,
		proxy: [
			{
				context: ['/api'],
				target: 'http://localhost:8080',
				onProxyRes: (proxyRes, req, res) => {
				  //console.log('onProxyRes');
				  proxyRes.on('close', () => {
					//console.log('on proxyRes close');
					if (!res.writableEnded) {
					  res.end();
					}
				  });
				  res.on('close', () => {
					//console.log('on res close');
					proxyRes.destroy();
				  });
				},
			  },
		  ],
	},
	resolve: {
		extensions: ['.js', '.ts', '.tsx'],
	},
	plugins: [],
	module: {
		rules: [
			{
				test: /\.tsx?$/,
				use: 'ts-loader',
				exclude: /node_modules/,
			},
			{
				test: /\.css$/i,
				use: ['style-loader', 'css-loader', 'postcss-loader'],
			},
		],
	},
};