//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_suggest_request_hints.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingSuggestRequestHints {
  /// Returns a new [MappingSuggestRequestHints] instance.
  MappingSuggestRequestHints({

     this.kind,

     this.description,

     this.sourceId,
  });

  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingSuggestRequestHints &&
      other.kind == kind &&
      other.description == description &&
      other.sourceId == sourceId;

    @override
    int get hashCode =>
        (kind == null ? 0 : kind.hashCode) +
        (description == null ? 0 : description.hashCode) +
        (sourceId == null ? 0 : sourceId.hashCode);

  factory MappingSuggestRequestHints.fromJson(Map<String, dynamic> json) => _$MappingSuggestRequestHintsFromJson(json);

  Map<String, dynamic> toJson() => _$MappingSuggestRequestHintsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

