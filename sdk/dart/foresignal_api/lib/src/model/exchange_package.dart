//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/package_manifest.dart';
import 'package:foresignal_api/src/model/exchange_package_deliveries_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exchange_package.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExchangePackage {
  /// Returns a new [ExchangePackage] instance.
  ExchangePackage({

    required  this.id,

    required  this.manifest,

    required  this.status,

     this.sizeBytes,

     this.deliveries,

     this.reductions,

    required  this.createdBy,

    required  this.createdAt,

     this.downloadHref,

     this.error,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'manifest',
    required: true,
    includeIfNull: false,
  )


  final PackageManifest manifest;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ExchangePackageStatusEnum status;



  @JsonKey(
    
    name: r'sizeBytes',
    required: false,
    includeIfNull: false,
  )


  final int? sizeBytes;



  @JsonKey(
    
    name: r'deliveries',
    required: false,
    includeIfNull: false,
  )


  final List<ExchangePackageDeliveriesInner>? deliveries;



      /// LOCAL_ONLY or private data excluded or reduced to derived statistics
  @JsonKey(
    
    name: r'reductions',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? reductions;



  @JsonKey(
    
    name: r'createdBy',
    required: true,
    includeIfNull: false,
  )


  final PrincipalSummary createdBy;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'downloadHref',
    required: false,
    includeIfNull: false,
  )


  final String? downloadHref;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExchangePackage &&
      other.id == id &&
      other.manifest == manifest &&
      other.status == status &&
      other.sizeBytes == sizeBytes &&
      other.deliveries == deliveries &&
      other.reductions == reductions &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt &&
      other.downloadHref == downloadHref &&
      other.error == error;

    @override
    int get hashCode =>
        id.hashCode +
        manifest.hashCode +
        status.hashCode +
        (sizeBytes == null ? 0 : sizeBytes.hashCode) +
        deliveries.hashCode +
        reductions.hashCode +
        createdBy.hashCode +
        createdAt.hashCode +
        (downloadHref == null ? 0 : downloadHref.hashCode) +
        (error == null ? 0 : error.hashCode);

  factory ExchangePackage.fromJson(Map<String, dynamic> json) => _$ExchangePackageFromJson(json);

  Map<String, dynamic> toJson() => _$ExchangePackageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExchangePackageStatusEnum {
@JsonValue(r'building')
building(r'building'),
@JsonValue(r'ready')
ready(r'ready'),
@JsonValue(r'delivering')
delivering(r'delivering'),
@JsonValue(r'delivered')
delivered(r'delivered'),
@JsonValue(r'failed')
failed(r'failed');

const ExchangePackageStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


