//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/workspace_viewport.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'context_summary_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ContextSummaryRequest {
  /// Returns a new [ContextSummaryRequest] instance.
  ContextSummaryRequest({

    required  this.viewport,

    required  this.timeRange,

     this.asOf,

     this.filters,

     this.layers,
  });

  @JsonKey(
    
    name: r'viewport',
    required: true,
    includeIfNull: false,
  )


  final WorkspaceViewport viewport;



  @JsonKey(
    
    name: r'timeRange',
    required: true,
    includeIfNull: false,
  )


  final TimeRange timeRange;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



  @JsonKey(
    
    name: r'filters',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? filters;



  @JsonKey(
    
    name: r'layers',
    required: false,
    includeIfNull: false,
  )


  final List<String>? layers;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ContextSummaryRequest &&
      other.viewport == viewport &&
      other.timeRange == timeRange &&
      other.asOf == asOf &&
      other.filters == filters &&
      other.layers == layers;

    @override
    int get hashCode =>
        viewport.hashCode +
        timeRange.hashCode +
        (asOf == null ? 0 : asOf.hashCode) +
        filters.hashCode +
        layers.hashCode;

  factory ContextSummaryRequest.fromJson(Map<String, dynamic> json) => _$ContextSummaryRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ContextSummaryRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

