//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/trigger_mute_input_one_of.dart';
import 'package:foresignal_api/src/model/trigger_mute_input_one_of1.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'trigger_mute_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TriggerMuteInput {
  /// Returns a new [TriggerMuteInput] instance.
  TriggerMuteInput({

    required  this.until,

    required  this.for_,
  });

  @JsonKey(
    
    name: r'until',
    required: true,
    includeIfNull: false,
  )


  final DateTime until;



      /// 1h–7d
  @JsonKey(
    
    name: r'for',
    required: true,
    includeIfNull: false,
  )


  final String for_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TriggerMuteInput &&
      other.until == until &&
      other.for_ == for_;

    @override
    int get hashCode =>
        until.hashCode +
        for_.hashCode;

  factory TriggerMuteInput.fromJson(Map<String, dynamic> json) => _$TriggerMuteInputFromJson(json);

  Map<String, dynamic> toJson() => _$TriggerMuteInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

