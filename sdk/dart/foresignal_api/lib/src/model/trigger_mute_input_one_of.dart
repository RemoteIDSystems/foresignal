//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'trigger_mute_input_one_of.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TriggerMuteInputOneOf {
  /// Returns a new [TriggerMuteInputOneOf] instance.
  TriggerMuteInputOneOf({

    required  this.until,
  });

  @JsonKey(
    
    name: r'until',
    required: true,
    includeIfNull: false,
  )


  final DateTime until;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TriggerMuteInputOneOf &&
      other.until == until;

    @override
    int get hashCode =>
        until.hashCode;

  factory TriggerMuteInputOneOf.fromJson(Map<String, dynamic> json) => _$TriggerMuteInputOneOfFromJson(json);

  Map<String, dynamic> toJson() => _$TriggerMuteInputOneOfToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

