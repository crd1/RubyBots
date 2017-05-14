2.times do
	$context.battlefield.move()
end
$context.battlefield.mine($context.battlefield.getMyPosition())
$context.battlefield.mine($context.battlefield.getMyPosition() + 1)
#keep context information..
data = $context.getStoredData()
if data == nil
	$context.storeData(1)
else
	$context.storeData(data + 1)
end