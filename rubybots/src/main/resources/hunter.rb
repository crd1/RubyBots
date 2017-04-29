1.times do
	$context.battlefield.move()
end
$context.battlefield.mine($context.battlefield.getMyPosition())
target=rand($context.battlefield.size)
detectedBot=$context.battlefield.whoIsAtPosition(target)
if detectedBot != nil
	$context.log("Bot #{detectedBot} at position #{target}.")
end
if detectedBot == $context.botNumber
	$context.log("OK, let's commit suicide!")
elsif detectedBot == nil
	$context.log("Just firing for fun.")
else
	$context.log("Aiming at bot #{detectedBot}.")
end
$context.battlefield.fire(target)
$context.battlefield.fire(rand($context.battlefield.size))