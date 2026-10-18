//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analysis_manifest_inputs.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalysisManifestInputs {
  /// Returns a new [AnalysisManifestInputs] instance.
  AnalysisManifestInputs({

     this.refs,

     this.query,

    required  this.count,

    required  this.hash,
  });

  @JsonKey(
    
    name: r'refs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? refs;



  @JsonKey(
    
    name: r'query',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? query;



  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;



  @JsonKey(
    
    name: r'hash',
    required: true,
    includeIfNull: false,
  )


  final String hash;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalysisManifestInputs &&
      other.refs == refs &&
      other.query == query &&
      other.count == count &&
      other.hash == hash;

    @override
    int get hashCode =>
        (refs == null ? 0 : refs.hashCode) +
        (query == null ? 0 : query.hashCode) +
        count.hashCode +
        hash.hashCode;

  factory AnalysisManifestInputs.fromJson(Map<String, dynamic> json) => _$AnalysisManifestInputsFromJson(json);

  Map<String, dynamic> toJson() => _$AnalysisManifestInputsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

