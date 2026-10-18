//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'timeline_bucket.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TimelineBucket {
  /// Returns a new [TimelineBucket] instance.
  TimelineBucket({

    required  this.t,

    required  this.total,

     this.counts,
  });

  @JsonKey(
    
    name: r't',
    required: true,
    includeIfNull: false,
  )


  final DateTime t;



  @JsonKey(
    
    name: r'total',
    required: true,
    includeIfNull: false,
  )


  final int total;



      /// Per kind or source when groupBy is set
  @JsonKey(
    
    name: r'counts',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? counts;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TimelineBucket &&
      other.t == t &&
      other.total == total &&
      other.counts == counts;

    @override
    int get hashCode =>
        t.hashCode +
        total.hashCode +
        counts.hashCode;

  factory TimelineBucket.fromJson(Map<String, dynamic> json) => _$TimelineBucketFromJson(json);

  Map<String, dynamic> toJson() => _$TimelineBucketToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

