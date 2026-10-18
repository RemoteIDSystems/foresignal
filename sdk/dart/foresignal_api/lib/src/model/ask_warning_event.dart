//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_warning_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskWarningEvent {
  /// Returns a new [AskWarningEvent] instance.
  AskWarningEvent({

    required  this.message,
  });

  @JsonKey(
    
    name: r'message',
    required: true,
    includeIfNull: false,
  )


  final String message;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskWarningEvent &&
      other.message == message;

    @override
    int get hashCode =>
        message.hashCode;

  factory AskWarningEvent.fromJson(Map<String, dynamic> json) => _$AskWarningEventFromJson(json);

  Map<String, dynamic> toJson() => _$AskWarningEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

