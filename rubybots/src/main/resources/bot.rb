2.times do
#	$context.battlefield.move()
end
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