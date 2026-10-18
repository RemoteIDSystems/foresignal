//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'snooze_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SnoozeInput {
  /// Returns a new [SnoozeInput] instance.
  SnoozeInput({

     this.until,

     this.for_,
  });

  @JsonKey(
    
    name: r'until',
    required: false,
    includeIfNull: false,
  )


  final DateTime? until;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'for',
    required: false,
    includeIfNull: false,
  )


  final String? for_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SnoozeInput &&
      other.until == until &&
      other.for_ == for_;

    @override
    int get hashCode =>
        until.hashCode +
        for_.hashCode;

  factory SnoozeInput.fromJson(Map<String, dynamic> json) => _$SnoozeInputFromJson(json);

  Map<String, dynamic> toJson() => _$SnoozeInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

