//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_delta_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskDeltaEvent {
  /// Returns a new [AskDeltaEvent] instance.
  AskDeltaEvent({

    required  this.text,
  });

  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskDeltaEvent &&
      other.text == text;

    @override
    int get hashCode =>
        text.hashCode;

  factory AskDeltaEvent.fromJson(Map<String, dynamic> json) => _$AskDeltaEventFromJson(json);

  Map<String, dynamic> toJson() => _$AskDeltaEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

