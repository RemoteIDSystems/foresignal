//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_sketch_batch_access_decisions_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSketchBatchAccessDecisionsInner {
  /// Returns a new [ChronicleSketchBatchAccessDecisionsInner] instance.
  ChronicleSketchBatchAccessDecisionsInner({

    required  this.requestId,

    required  this.status,

     this.deliveredAs,

     this.packageId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'requestId',
    required: true,
    includeIfNull: false,
  )


  final String requestId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ChronicleSketchBatchAccessDecisionsInnerStatusEnum status;



  @JsonKey(
    
    name: r'deliveredAs',
    required: false,
    includeIfNull: false,
  )


  final ChronicleSketchBatchAccessDecisionsInnerDeliveredAsEnum? deliveredAs;



      /// ULID identifier
  @JsonKey(
    
    name: r'packageId',
    required: false,
    includeIfNull: false,
  )


  final String? packageId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSketchBatchAccessDecisionsInner &&
      other.requestId == requestId &&
      other.status == status &&
      other.deliveredAs == deliveredAs &&
      other.packageId == packageId;

    @override
    int get hashCode =>
        requestId.hashCode +
        status.hashCode +
        (deliveredAs == null ? 0 : deliveredAs.hashCode) +
        (packageId == null ? 0 : packageId.hashCode);

  factory ChronicleSketchBatchAccessDecisionsInner.fromJson(Map<String, dynamic> json) => _$ChronicleSketchBatchAccessDecisionsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSketchBatchAccessDecisionsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleSketchBatchAccessDecisionsInnerStatusEnum {
@JsonValue(r'granted')
granted(r'granted'),
@JsonValue(r'declined')
declined(r'declined');

const ChronicleSketchBatchAccessDecisionsInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChronicleSketchBatchAccessDecisionsInnerDeliveredAsEnum {
@JsonValue(r'report')
report(r'report'),
@JsonValue(r'chronicle-extract')
chronicleExtract(r'chronicle-extract');

const ChronicleSketchBatchAccessDecisionsInnerDeliveredAsEnum(this.value);

final String value;

@override
String toString() => value;
}


