//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/mapping_test_result_samples_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_test_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingTestResult {
  /// Returns a new [MappingTestResult] instance.
  MappingTestResult({

    required  this.mappingSource,

    required  this.samples,
  });

  @JsonKey(
    
    name: r'mappingSource',
    required: true,
    includeIfNull: false,
  )


  final MappingTestResultMappingSourceEnum mappingSource;



  @JsonKey(
    
    name: r'samples',
    required: true,
    includeIfNull: false,
  )


  final List<MappingTestResultSamplesInner> samples;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingTestResult &&
      other.mappingSource == mappingSource &&
      other.samples == samples;

    @override
    int get hashCode =>
        mappingSource.hashCode +
        samples.hashCode;

  factory MappingTestResult.fromJson(Map<String, dynamic> json) => _$MappingTestResultFromJson(json);

  Map<String, dynamic> toJson() => _$MappingTestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MappingTestResultMappingSourceEnum {
@JsonValue(r'stored')
stored(r'stored'),
@JsonValue(r'proposed')
proposed(r'proposed');

const MappingTestResultMappingSourceEnum(this.value);

final String value;

@override
String toString() => value;
}


