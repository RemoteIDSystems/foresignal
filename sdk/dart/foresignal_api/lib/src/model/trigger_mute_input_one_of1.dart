//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'trigger_mute_input_one_of1.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TriggerMuteInputOneOf1 {
  /// Returns a new [TriggerMuteInputOneOf1] instance.
  TriggerMuteInputOneOf1({

    required  this.for_,
  });

      /// 1h–7d
  @JsonKey(
    
    name: r'for',
    required: true,
    includeIfNull: false,
  )


  final String for_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TriggerMuteInputOneOf1 &&
      other.for_ == for_;

    @override
    int get hashCode =>
        for_.hashCode;

  factory TriggerMuteInputOneOf1.fromJson(Map<String, dynamic> json) => _$TriggerMuteInputOneOf1FromJson(json);

  Map<String, dynamic> toJson() => _$TriggerMuteInputOneOf1ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

