//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'legend_tick.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LegendTick {
  /// Returns a new [LegendTick] instance.
  LegendTick({

    required  this.value,

    required  this.label,
  });

  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: false,
  )


  final num value;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final String label;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LegendTick &&
      other.value == value &&
      other.label == label;

    @override
    int get hashCode =>
        value.hashCode +
        label.hashCode;

  factory LegendTick.fromJson(Map<String, dynamic> json) => _$LegendTickFromJson(json);

  Map<String, dynamic> toJson() => _$LegendTickToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

