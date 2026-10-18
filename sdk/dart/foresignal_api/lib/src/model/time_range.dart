//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'time_range.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TimeRange {
  /// Returns a new [TimeRange] instance.
  TimeRange({

    required  this.start,

     this.end,
  });

  @JsonKey(
    
    name: r'start',
    required: true,
    includeIfNull: false,
  )


  final DateTime start;



  @JsonKey(
    
    name: r'end',
    required: false,
    includeIfNull: false,
  )


  final DateTime? end;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TimeRange &&
      other.start == start &&
      other.end == end;

    @override
    int get hashCode =>
        start.hashCode +
        (end == null ? 0 : end.hashCode);

  factory TimeRange.fromJson(Map<String, dynamic> json) => _$TimeRangeFromJson(json);

  Map<String, dynamic> toJson() => _$TimeRangeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

