//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_sketch_package_capture_span.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_sketch_package.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSketchPackage {
  /// Returns a new [ChronicleSketchPackage] instance.
  ChronicleSketchPackage({

    required  this.sketchId,

    required  this.instanceId,

    required  this.networkId,

    required  this.keyTypes,

    required  this.minhash,

    required  this.sizeBand,

    required  this.captureSpan,

     this.title,

     this.ownerHandle,

    required  this.tlp,

    required  this.issuedAt,

     this.expiresAt,
  });

      /// Random per chronicle per salt
  @JsonKey(
    
    name: r'sketchId',
    required: true,
    includeIfNull: false,
  )


  final String sketchId;



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
    
    name: r'keyTypes',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleSketchPackageKeyTypesEnum> keyTypes;



      /// Key type → 128 MinHash values (base64 of 128 big-endian 64-bit integers)
  @JsonKey(
    
    name: r'minhash',
    required: true,
    includeIfNull: false,
  )


  final Map<String, String> minhash;



      /// Key type → log2 bucket of the set size
  @JsonKey(
    
    name: r'sizeBand',
    required: true,
    includeIfNull: false,
  )


  final Map<String, int> sizeBand;



  @JsonKey(
    
    name: r'captureSpan',
    required: true,
    includeIfNull: false,
  )


  final ChronicleSketchPackageCaptureSpan captureSpan;



      /// Only if revealed
  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



      /// Only if revealed
  @JsonKey(
    
    name: r'ownerHandle',
    required: false,
    includeIfNull: false,
  )


  final String? ownerHandle;



  @JsonKey(
    
    name: r'tlp',
    required: true,
    includeIfNull: false,
  )


  final Tlp tlp;



  @JsonKey(
    
    name: r'issuedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime issuedAt;



  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSketchPackage &&
      other.sketchId == sketchId &&
      other.instanceId == instanceId &&
      other.networkId == networkId &&
      other.keyTypes == keyTypes &&
      other.minhash == minhash &&
      other.sizeBand == sizeBand &&
      other.captureSpan == captureSpan &&
      other.title == title &&
      other.ownerHandle == ownerHandle &&
      other.tlp == tlp &&
      other.issuedAt == issuedAt &&
      other.expiresAt == expiresAt;

    @override
    int get hashCode =>
        sketchId.hashCode +
        instanceId.hashCode +
        networkId.hashCode +
        keyTypes.hashCode +
        minhash.hashCode +
        sizeBand.hashCode +
        captureSpan.hashCode +
        (title == null ? 0 : title.hashCode) +
        (ownerHandle == null ? 0 : ownerHandle.hashCode) +
        tlp.hashCode +
        issuedAt.hashCode +
        expiresAt.hashCode;

  factory ChronicleSketchPackage.fromJson(Map<String, dynamic> json) => _$ChronicleSketchPackageFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSketchPackageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleSketchPackageKeyTypesEnum {
@JsonValue(r'evidence')
evidence(r'evidence'),
@JsonValue(r'entities')
entities(r'entities'),
@JsonValue(r'area-time')
areaTime(r'area-time'),
@JsonValue(r'topics')
topics(r'topics');

const ChronicleSketchPackageKeyTypesEnum(this.value);

final String value;

@override
String toString() => value;
}


