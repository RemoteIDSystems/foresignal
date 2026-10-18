// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exchange_package.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExchangePackageCWProxy {
  ExchangePackage id(String id);

  ExchangePackage manifest(PackageManifest manifest);

  ExchangePackage status(ExchangePackageStatusEnum status);

  ExchangePackage sizeBytes(int? sizeBytes);

  ExchangePackage deliveries(List<ExchangePackageDeliveriesInner>? deliveries);

  ExchangePackage reductions(List<Caveat>? reductions);

  ExchangePackage createdBy(PrincipalSummary createdBy);

  ExchangePackage createdAt(DateTime createdAt);

  ExchangePackage downloadHref(String? downloadHref);

  ExchangePackage error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExchangePackage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExchangePackage(...).copyWith(id: 12, name: "My name")
  /// ```
  ExchangePackage call({
    String id,
    PackageManifest manifest,
    ExchangePackageStatusEnum status,
    int? sizeBytes,
    List<ExchangePackageDeliveriesInner>? deliveries,
    List<Caveat>? reductions,
    PrincipalSummary createdBy,
    DateTime createdAt,
    String? downloadHref,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExchangePackage.copyWith(...)` or call `instanceOfExchangePackage.copyWith.fieldName(value)` for a single field.
class _$ExchangePackageCWProxyImpl implements _$ExchangePackageCWProxy {
  const _$ExchangePackageCWProxyImpl(this._value);

  final ExchangePackage _value;

  @override
  ExchangePackage id(String id) => call(id: id);

  @override
  ExchangePackage manifest(PackageManifest manifest) =>
      call(manifest: manifest);

  @override
  ExchangePackage status(ExchangePackageStatusEnum status) =>
      call(status: status);

  @override
  ExchangePackage sizeBytes(int? sizeBytes) => call(sizeBytes: sizeBytes);

  @override
  ExchangePackage deliveries(
    List<ExchangePackageDeliveriesInner>? deliveries,
  ) => call(deliveries: deliveries);

  @override
  ExchangePackage reductions(List<Caveat>? reductions) =>
      call(reductions: reductions);

  @override
  ExchangePackage createdBy(PrincipalSummary createdBy) =>
      call(createdBy: createdBy);

  @override
  ExchangePackage createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  ExchangePackage downloadHref(String? downloadHref) =>
      call(downloadHref: downloadHref);

  @override
  ExchangePackage error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExchangePackage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExchangePackage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExchangePackage call({
    Object? id = const $CopyWithPlaceholder(),
    Object? manifest = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? sizeBytes = const $CopyWithPlaceholder(),
    Object? deliveries = const $CopyWithPlaceholder(),
    Object? reductions = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? downloadHref = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return ExchangePackage(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      manifest: manifest == const $CopyWithPlaceholder() || manifest == null
          ? _value.manifest
          // ignore: cast_nullable_to_non_nullable
          : manifest as PackageManifest,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ExchangePackageStatusEnum,
      sizeBytes: sizeBytes == const $CopyWithPlaceholder()
          ? _value.sizeBytes
          // ignore: cast_nullable_to_non_nullable
          : sizeBytes as int?,
      deliveries: deliveries == const $CopyWithPlaceholder()
          ? _value.deliveries
          // ignore: cast_nullable_to_non_nullable
          : deliveries as List<ExchangePackageDeliveriesInner>?,
      reductions: reductions == const $CopyWithPlaceholder()
          ? _value.reductions
          // ignore: cast_nullable_to_non_nullable
          : reductions as List<Caveat>?,
      createdBy: createdBy == const $CopyWithPlaceholder() || createdBy == null
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as PrincipalSummary,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      downloadHref: downloadHref == const $CopyWithPlaceholder()
          ? _value.downloadHref
          // ignore: cast_nullable_to_non_nullable
          : downloadHref as String?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $ExchangePackageCopyWith on ExchangePackage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExchangePackage.copyWith(...)` or `instanceOfExchangePackage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExchangePackageCWProxy get copyWith => _$ExchangePackageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExchangePackage _$ExchangePackageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExchangePackage', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'manifest', 'status', 'createdBy', 'createdAt'],
  );
  final val = ExchangePackage(
    id: $checkedConvert('id', (v) => v as String),
    manifest: $checkedConvert(
      'manifest',
      (v) => PackageManifest.fromJson(v as Map<String, dynamic>),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ExchangePackageStatusEnumEnumMap, v),
    ),
    sizeBytes: $checkedConvert('sizeBytes', (v) => (v as num?)?.toInt()),
    deliveries: $checkedConvert(
      'deliveries',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => ExchangePackageDeliveriesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    reductions: $checkedConvert(
      'reductions',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    createdBy: $checkedConvert(
      'createdBy',
      (v) => PrincipalSummary.fromJson(v as Map<String, dynamic>),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    downloadHref: $checkedConvert('downloadHref', (v) => v as String?),
    error: $checkedConvert('error', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ExchangePackageToJson(ExchangePackage instance) =>
    <String, dynamic>{
      'id': instance.id,
      'manifest': instance.manifest.toJson(),
      'status': _$ExchangePackageStatusEnumEnumMap[instance.status]!,
      'sizeBytes': ?instance.sizeBytes,
      'deliveries': ?instance.deliveries?.map((e) => e.toJson()).toList(),
      'reductions': ?instance.reductions?.map((e) => e.toJson()).toList(),
      'createdBy': instance.createdBy.toJson(),
      'createdAt': instance.createdAt.toIso8601String(),
      'downloadHref': ?instance.downloadHref,
      'error': ?instance.error,
    };

const _$ExchangePackageStatusEnumEnumMap = {
  ExchangePackageStatusEnum.building: 'building',
  ExchangePackageStatusEnum.ready: 'ready',
  ExchangePackageStatusEnum.delivering: 'delivering',
  ExchangePackageStatusEnum.delivered: 'delivered',
  ExchangePackageStatusEnum.failed: 'failed',
};
