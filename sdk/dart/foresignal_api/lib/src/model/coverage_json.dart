//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'coverage_json.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CoverageJson {
  /// Returns a new [CoverageJson] instance.
  CoverageJson({

    required  this.type,

     this.domain,

     this.parameters,

     this.ranges,

     this.coverages,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final CoverageJsonTypeEnum type;



      /// Domain axes and referencing
  @JsonKey(
    
    name: r'domain',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? domain;



  @JsonKey(
    
    name: r'parameters',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Map<String, Object>>? parameters;



  @JsonKey(
    
    name: r'ranges',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Map<String, Object>>? ranges;



  @JsonKey(
    
    name: r'coverages',
    required: false,
    includeIfNull: false,
  )


  final List<Map<String, Object>>? coverages;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CoverageJson &&
      other.type == type &&
      other.domain == domain &&
      other.parameters == parameters &&
      other.ranges == ranges &&
      other.coverages == coverages;

    @override
    int get hashCode =>
        type.hashCode +
        domain.hashCode +
        parameters.hashCode +
        ranges.hashCode +
        coverages.hashCode;

  factory CoverageJson.fromJson(Map<String, dynamic> json) => _$CoverageJsonFromJson(json);

  Map<String, dynamic> toJson() => _$CoverageJsonToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CoverageJsonTypeEnum {
@JsonValue(r'Coverage')
coverage(r'Coverage'),
@JsonValue(r'CoverageCollection')
coverageCollection(r'CoverageCollection');

const CoverageJsonTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


