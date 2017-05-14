$context.battlefield.fire($context.battlefield.getMyPosition() + 1) # clear the way
$context.battlefield.move() # move one step
1.times do
	target=rand($context.battlefield.size)
	detectedBot=$context.battlefield.whoIsAtPosition(target)
	if detectedBot == $context.botNumber
		$context.log("OK, let's commit suicide!")
	elsif detectedBot == nil
		$context.log("Just firing for fun.")
	else
		$context.log("Aiming at bot #{detectedBot}.")
	end
	$context.battlefield.fire(target) # fire anywhere
end
$context.battlefield.mine($context.battlefield.getMyPosition()) # mine the position we were at