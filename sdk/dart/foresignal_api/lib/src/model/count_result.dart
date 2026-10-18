//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'count_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CountResult {
  /// Returns a new [CountResult] instance.
  CountResult({

    required  this.count,

    required  this.exact,

    required  this.watermarkSeq,
  });

  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;



      /// False above 1,000,000
  @JsonKey(
    
    name: r'exact',
    required: true,
    includeIfNull: false,
  )


  final bool exact;



  @JsonKey(
    
    name: r'watermarkSeq',
    required: true,
    includeIfNull: false,
  )


  final int watermarkSeq;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CountResult &&
      other.count == count &&
      other.exact == exact &&
      other.watermarkSeq == watermarkSeq;

    @override
    int get hashCode =>
        count.hashCode +
        exact.hashCode +
        watermarkSeq.hashCode;

  factory CountResult.fromJson(Map<String, dynamic> json) => _$CountResultFromJson(json);

  Map<String, dynamic> toJson() => _$CountResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

