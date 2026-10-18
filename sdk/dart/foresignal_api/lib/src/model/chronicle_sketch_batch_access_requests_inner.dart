//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_sketch_batch_access_requests_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSketchBatchAccessRequestsInner {
  /// Returns a new [ChronicleSketchBatchAccessRequestsInner] instance.
  ChronicleSketchBatchAccessRequestsInner({

    required  this.requestId,

     this.linkId,

    required  this.sketchId,

    required  this.requesterHandle,

     this.message,

     this.offerShare,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'requestId',
    required: true,
    includeIfNull: false,
  )


  final String requestId;



      /// ULID identifier
  @JsonKey(
    
    name: r'linkId',
    required: false,
    includeIfNull: false,
  )


  final String? linkId;



      /// The receiving instance's sketch id
  @JsonKey(
    
    name: r'sketchId',
    required: true,
    includeIfNull: false,
  )


  final String sketchId;



  @JsonKey(
    
    name: r'requesterHandle',
    required: true,
    includeIfNull: false,
  )


  final String requesterHandle;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



      /// The requester offers to share a chronicle of its own
  @JsonKey(
    
    name: r'offerShare',
    required: false,
    includeIfNull: false,
  )


  final bool? offerShare;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSketchBatchAccessRequestsInner &&
      other.requestId == requestId &&
      other.linkId == linkId &&
      other.sketchId == sketchId &&
      other.requesterHandle == requesterHandle &&
      other.message == message &&
      other.offerShare == offerShare;

    @override
    int get hashCode =>
        requestId.hashCode +
        linkId.hashCode +
        sketchId.hashCode +
        requesterHandle.hashCode +
        (message == null ? 0 : message.hashCode) +
        offerShare.hashCode;

  factory ChronicleSketchBatchAccessRequestsInner.fromJson(Map<String, dynamic> json) => _$ChronicleSketchBatchAccessRequestsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSketchBatchAccessRequestsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

