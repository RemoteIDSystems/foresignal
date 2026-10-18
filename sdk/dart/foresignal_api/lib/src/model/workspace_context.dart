//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/workspace_viewport.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:foresignal_api/src/model/workspace_context_playback.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'workspace_context.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WorkspaceContext {
  /// Returns a new [WorkspaceContext] instance.
  WorkspaceContext({

     this.selectedRefs,

     this.focusRef,

     this.timeRange,

     this.asOf,

     this.playback,

     this.viewport,

     this.activeSituationId,

     this.activeHypothesisSetId,

     this.filters,

     this.layers,
  });

  @JsonKey(
    
    name: r'selectedRefs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? selectedRefs;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'focusRef',
    required: false,
    includeIfNull: false,
  )


  final String? focusRef;



  @JsonKey(
    
    name: r'timeRange',
    required: false,
    includeIfNull: false,
  )


  final TimeRange? timeRange;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



  @JsonKey(
    
    name: r'playback',
    required: false,
    includeIfNull: false,
  )


  final WorkspaceContextPlayback? playback;



  @JsonKey(
    
    name: r'viewport',
    required: false,
    includeIfNull: false,
  )


  final WorkspaceViewport? viewport;



      /// ULID identifier
  @JsonKey(
    
    name: r'activeSituationId',
    required: false,
    includeIfNull: false,
  )


  final String? activeSituationId;



      /// ULID identifier
  @JsonKey(
    
    name: r'activeHypothesisSetId',
    required: false,
    includeIfNull: false,
  )


  final String? activeHypothesisSetId;



      /// Simple values the clients agree on; validated for shape and size only
  @JsonKey(
    
    name: r'filters',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? filters;



      /// Opaque layer ids (source ids, kinds, `observability:{family}`)
  @JsonKey(
    
    name: r'layers',
    required: false,
    includeIfNull: false,
  )


  final List<String>? layers;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WorkspaceContext &&
      other.selectedRefs == selectedRefs &&
      other.focusRef == focusRef &&
      other.timeRange == timeRange &&
      other.asOf == asOf &&
      other.playback == playback &&
      other.viewport == viewport &&
      other.activeSituationId == activeSituationId &&
      other.activeHypothesisSetId == activeHypothesisSetId &&
      other.filters == filters &&
      other.layers == layers;

    @override
    int get hashCode =>
        selectedRefs.hashCode +
        (focusRef == null ? 0 : focusRef.hashCode) +
        (timeRange == null ? 0 : timeRange.hashCode) +
        (asOf == null ? 0 : asOf.hashCode) +
        (playback == null ? 0 : playback.hashCode) +
        (viewport == null ? 0 : viewport.hashCode) +
        (activeSituationId == null ? 0 : activeSituationId.hashCode) +
        (activeHypothesisSetId == null ? 0 : activeHypothesisSetId.hashCode) +
        filters.hashCode +
        layers.hashCode;

  factory WorkspaceContext.fromJson(Map<String, dynamic> json) => _$WorkspaceContextFromJson(json);

  Map<String, dynamic> toJson() => _$WorkspaceContextToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

