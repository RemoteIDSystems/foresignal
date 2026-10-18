//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_search.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoSearch {
  /// Returns a new [SystemInfoSearch] instance.
  SystemInfoSearch({

     this.semanticEnabled,

     this.reason,

     this.documents,

     this.backlog,

     this.activeModel,

     this.reindexState,
  });

  @JsonKey(
    
    name: r'semanticEnabled',
    required: false,
    includeIfNull: false,
  )


  final bool? semanticEnabled;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



  @JsonKey(
    
    name: r'documents',
    required: false,
    includeIfNull: false,
  )


  final int? documents;



  @JsonKey(
    
    name: r'backlog',
    required: false,
    includeIfNull: false,
  )


  final int? backlog;



  @JsonKey(
    
    name: r'activeModel',
    required: false,
    includeIfNull: false,
  )


  final String? activeModel;



  @JsonKey(
    
    name: r'reindexState',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoSearchReindexStateEnum? reindexState;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoSearch &&
      other.semanticEnabled == semanticEnabled &&
      other.reason == reason &&
      other.documents == documents &&
      other.backlog == backlog &&
      other.activeModel == activeModel &&
      other.reindexState == reindexState;

    @override
    int get hashCode =>
        semanticEnabled.hashCode +
        (reason == null ? 0 : reason.hashCode) +
        documents.hashCode +
        backlog.hashCode +
        (activeModel == null ? 0 : activeModel.hashCode) +
        reindexState.hashCode;

  factory SystemInfoSearch.fromJson(Map<String, dynamic> json) => _$SystemInfoSearchFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoSearchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SystemInfoSearchReindexStateEnum {
@JsonValue(r'idle')
idle(r'idle'),
@JsonValue(r'backfilling')
backfilling(r'backfilling'),
@JsonValue(r'indexing')
indexing(r'indexing'),
@JsonValue(r'swapping')
swapping(r'swapping');

const SystemInfoSearchReindexStateEnum(this.value);

final String value;

@override
String toString() => value;
}


