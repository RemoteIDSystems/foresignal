//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/search_reindex_state.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'search_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SearchStatus {
  /// Returns a new [SearchStatus] instance.
  SearchStatus({

    required  this.semanticEnabled,

     this.reason,

     this.model,

     this.dimensions,

    required  this.documents,

     this.backlog,

     this.reindexing,

     this.activeModel,

     this.reindex,

     this.degraded,
  });

  @JsonKey(
    
    name: r'semanticEnabled',
    required: true,
    includeIfNull: false,
  )


  final bool semanticEnabled;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



  @JsonKey(
    
    name: r'model',
    required: false,
    includeIfNull: false,
  )


  final String? model;



  @JsonKey(
    
    name: r'dimensions',
    required: false,
    includeIfNull: false,
  )


  final int? dimensions;



  @JsonKey(
    
    name: r'documents',
    required: true,
    includeIfNull: false,
  )


  final int documents;



  @JsonKey(
    
    name: r'backlog',
    required: false,
    includeIfNull: false,
  )


  final int? backlog;



  @JsonKey(
    
    name: r'reindexing',
    required: false,
    includeIfNull: false,
  )


  final bool? reindexing;



      /// Active model id `<providerId>:<model>:<dimensions>`
  @JsonKey(
    
    name: r'activeModel',
    required: false,
    includeIfNull: false,
  )


  final String? activeModel;



  @JsonKey(
    
    name: r'reindex',
    required: false,
    includeIfNull: false,
  )


  final SearchReindexState? reindex;



      /// True while semantic search falls back to text (e.g. outgoing model no longer callable)
  @JsonKey(
    
    name: r'degraded',
    required: false,
    includeIfNull: false,
  )


  final bool? degraded;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SearchStatus &&
      other.semanticEnabled == semanticEnabled &&
      other.reason == reason &&
      other.model == model &&
      other.dimensions == dimensions &&
      other.documents == documents &&
      other.backlog == backlog &&
      other.reindexing == reindexing &&
      other.activeModel == activeModel &&
      other.reindex == reindex &&
      other.degraded == degraded;

    @override
    int get hashCode =>
        semanticEnabled.hashCode +
        (reason == null ? 0 : reason.hashCode) +
        (model == null ? 0 : model.hashCode) +
        (dimensions == null ? 0 : dimensions.hashCode) +
        documents.hashCode +
        backlog.hashCode +
        reindexing.hashCode +
        (activeModel == null ? 0 : activeModel.hashCode) +
        reindex.hashCode +
        degraded.hashCode;

  factory SearchStatus.fromJson(Map<String, dynamic> json) => _$SearchStatusFromJson(json);

  Map<String, dynamic> toJson() => _$SearchStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

