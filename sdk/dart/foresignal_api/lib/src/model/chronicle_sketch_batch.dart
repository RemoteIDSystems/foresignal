//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_sketch_batch_access_decisions_inner.dart';
import 'package:foresignal_api/src/model/chronicle_sketch_batch_access_requests_inner.dart';
import 'package:foresignal_api/src/model/chronicle_sketch_package.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_sketch_batch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSketchBatch {
  /// Returns a new [ChronicleSketchBatch] instance.
  ChronicleSketchBatch({

    required  this.instanceId,

    required  this.networkId,

    required  this.sketches,

     this.withdrawn,

     this.accessRequests,

     this.accessDecisions,
  });

  @JsonKey(
    
    name: r'instanceId',
    required: true,
    includeIfNull: false,
  )


  final String instanceId;



  @JsonKey(
    
    name: r'networkId',
    required: true,
    includeIfNull: false,
  )


  final String networkId;



  @JsonKey(
    
    name: r'sketches',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleSketchPackage> sketches;



      /// Sketch ids no longer valid
  @JsonKey(
    
    name: r'withdrawn',
    required: false,
    includeIfNull: false,
  )


  final List<String>? withdrawn;



      /// Access requests relayed to the instance holding the sketch (134); never the requester's chronicle
  @JsonKey(
    
    name: r'accessRequests',
    required: false,
    includeIfNull: false,
  )


  final List<ChronicleSketchBatchAccessRequestsInner>? accessRequests;



      /// Owners' decisions on relayed requests, sent back to the requester's instance (134); status only
  @JsonKey(
    
    name: r'accessDecisions',
    required: false,
    includeIfNull: false,
  )


  final List<ChronicleSketchBatchAccessDecisionsInner>? accessDecisions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSketchBatch &&
      other.instanceId == instanceId &&
      other.networkId == networkId &&
      other.sketches == sketches &&
      other.withdrawn == withdrawn &&
      other.accessRequests == accessRequests &&
      other.accessDecisions == accessDecisions;

    @override
    int get hashCode =>
        instanceId.hashCode +
        networkId.hashCode +
        sketches.hashCode +
        withdrawn.hashCode +
        accessRequests.hashCode +
        accessDecisions.hashCode;

  factory ChronicleSketchBatch.fromJson(Map<String, dynamic> json) => _$ChronicleSketchBatchFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSketchBatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

