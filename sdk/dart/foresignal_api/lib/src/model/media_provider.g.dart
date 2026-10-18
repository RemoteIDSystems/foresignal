// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media_provider.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MediaProviderCWProxy {
  MediaProvider id(MediaProviderIdEnum id);

  MediaProvider name(String name);

  MediaProvider enabled(bool enabled);

  MediaProvider defaultEnabled(bool? defaultEnabled);

  MediaProvider licence(String licence);

  MediaProvider licenceUrl(String? licenceUrl);

  MediaProvider attribution(String? attribution);

  MediaProvider licenceAcknowledged(bool licenceAcknowledged);

  MediaProvider acknowledgedBy(String? acknowledgedBy);

  MediaProvider acknowledgedAt(DateTime? acknowledgedAt);

  MediaProvider egress(MediaProviderEgressEnum egress);

  MediaProvider permissionRecorded(bool? permissionRecorded);

  MediaProvider credentialSet(bool? credentialSet);

  MediaProvider lastImportAt(DateTime? lastImportAt);

  MediaProvider lastImportStatus(
    MediaProviderLastImportStatusEnum? lastImportStatus,
  );

  MediaProvider ratingsCount(int? ratingsCount);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaProvider(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaProvider(...).copyWith(id: 12, name: "My name")
  /// ```
  MediaProvider call({
    MediaProviderIdEnum id,
    String name,
    bool enabled,
    bool? defaultEnabled,
    String licence,
    String? licenceUrl,
    String? attribution,
    bool licenceAcknowledged,
    String? acknowledgedBy,
    DateTime? acknowledgedAt,
    MediaProviderEgressEnum egress,
    bool? permissionRecorded,
    bool? credentialSet,
    DateTime? lastImportAt,
    MediaProviderLastImportStatusEnum? lastImportStatus,
    int? ratingsCount,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMediaProvider.copyWith(...)` or call `instanceOfMediaProvider.copyWith.fieldName(value)` for a single field.
class _$MediaProviderCWProxyImpl implements _$MediaProviderCWProxy {
  const _$MediaProviderCWProxyImpl(this._value);

  final MediaProvider _value;

  @override
  MediaProvider id(MediaProviderIdEnum id) => call(id: id);

  @override
  MediaProvider name(String name) => call(name: name);

  @override
  MediaProvider enabled(bool enabled) => call(enabled: enabled);

  @override
  MediaProvider defaultEnabled(bool? defaultEnabled) =>
      call(defaultEnabled: defaultEnabled);

  @override
  MediaProvider licence(String licence) => call(licence: licence);

  @override
  MediaProvider licenceUrl(String? licenceUrl) => call(licenceUrl: licenceUrl);

  @override
  MediaProvider attribution(String? attribution) =>
      call(attribution: attribution);

  @override
  MediaProvider licenceAcknowledged(bool licenceAcknowledged) =>
      call(licenceAcknowledged: licenceAcknowledged);

  @override
  MediaProvider acknowledgedBy(String? acknowledgedBy) =>
      call(acknowledgedBy: acknowledgedBy);

  @override
  MediaProvider acknowledgedAt(DateTime? acknowledgedAt) =>
      call(acknowledgedAt: acknowledgedAt);

  @override
  MediaProvider egress(MediaProviderEgressEnum egress) => call(egress: egress);

  @override
  MediaProvider permissionRecorded(bool? permissionRecorded) =>
      call(permissionRecorded: permissionRecorded);

  @override
  MediaProvider credentialSet(bool? credentialSet) =>
      call(credentialSet: credentialSet);

  @override
  MediaProvider lastImportAt(DateTime? lastImportAt) =>
      call(lastImportAt: lastImportAt);

  @override
  MediaProvider lastImportStatus(
    MediaProviderLastImportStatusEnum? lastImportStatus,
  ) => call(lastImportStatus: lastImportStatus);

  @override
  MediaProvider ratingsCount(int? ratingsCount) =>
      call(ratingsCount: ratingsCount);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaProvider(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaProvider(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MediaProvider call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? defaultEnabled = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? licenceUrl = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? licenceAcknowledged = const $CopyWithPlaceholder(),
    Object? acknowledgedBy = const $CopyWithPlaceholder(),
    Object? acknowledgedAt = const $CopyWithPlaceholder(),
    Object? egress = const $CopyWithPlaceholder(),
    Object? permissionRecorded = const $CopyWithPlaceholder(),
    Object? credentialSet = const $CopyWithPlaceholder(),
    Object? lastImportAt = const $CopyWithPlaceholder(),
    Object? lastImportStatus = const $CopyWithPlaceholder(),
    Object? ratingsCount = const $CopyWithPlaceholder(),
  }) {
    return MediaProvider(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as MediaProviderIdEnum,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      defaultEnabled: defaultEnabled == const $CopyWithPlaceholder()
          ? _value.defaultEnabled
          // ignore: cast_nullable_to_non_nullable
          : defaultEnabled as bool?,
      licence: licence == const $CopyWithPlaceholder() || licence == null
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String,
      licenceUrl: licenceUrl == const $CopyWithPlaceholder()
          ? _value.licenceUrl
          // ignore: cast_nullable_to_non_nullable
          : licenceUrl as String?,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String?,
      licenceAcknowledged:
          licenceAcknowledged == const $CopyWithPlaceholder() ||
              licenceAcknowledged == null
          ? _value.licenceAcknowledged
          // ignore: cast_nullable_to_non_nullable
          : licenceAcknowledged as bool,
      acknowledgedBy: acknowledgedBy == const $CopyWithPlaceholder()
          ? _value.acknowledgedBy
          // ignore: cast_nullable_to_non_nullable
          : acknowledgedBy as String?,
      acknowledgedAt: acknowledgedAt == const $CopyWithPlaceholder()
          ? _value.acknowledgedAt
          // ignore: cast_nullable_to_non_nullable
          : acknowledgedAt as DateTime?,
      egress: egress == const $CopyWithPlaceholder() || egress == null
          ? _value.egress
          // ignore: cast_nullable_to_non_nullable
          : egress as MediaProviderEgressEnum,
      permissionRecorded: permissionRecorded == const $CopyWithPlaceholder()
          ? _value.permissionRecorded
          // ignore: cast_nullable_to_non_nullable
          : permissionRecorded as bool?,
      credentialSet: credentialSet == const $CopyWithPlaceholder()
          ? _value.credentialSet
          // ignore: cast_nullable_to_non_nullable
          : credentialSet as bool?,
      lastImportAt: lastImportAt == const $CopyWithPlaceholder()
          ? _value.lastImportAt
          // ignore: cast_nullable_to_non_nullable
          : lastImportAt as DateTime?,
      lastImportStatus: lastImportStatus == const $CopyWithPlaceholder()
          ? _value.lastImportStatus
          // ignore: cast_nullable_to_non_nullable
          : lastImportStatus as MediaProviderLastImportStatusEnum?,
      ratingsCount: ratingsCount == const $CopyWithPlaceholder()
          ? _value.ratingsCount
          // ignore: cast_nullable_to_non_nullable
          : ratingsCount as int?,
    );
  }
}

extension $MediaProviderCopyWith on MediaProvider {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMediaProvider.copyWith(...)` or `instanceOfMediaProvider.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MediaProviderCWProxy get copyWith => _$MediaProviderCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MediaProvider _$MediaProviderFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MediaProvider', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'enabled',
      'licence',
      'licenceAcknowledged',
      'egress',
    ],
  );
  final val = MediaProvider(
    id: $checkedConvert(
      'id',
      (v) => $enumDecode(_$MediaProviderIdEnumEnumMap, v),
    ),
    name: $checkedConvert('name', (v) => v as String),
    enabled: $checkedConvert('enabled', (v) => v as bool),
    defaultEnabled: $checkedConvert('defaultEnabled', (v) => v as bool?),
    licence: $checkedConvert('licence', (v) => v as String),
    licenceUrl: $checkedConvert('licenceUrl', (v) => v as String?),
    attribution: $checkedConvert('attribution', (v) => v as String?),
    licenceAcknowledged: $checkedConvert(
      'licenceAcknowledged',
      (v) => v as bool,
    ),
    acknowledgedBy: $checkedConvert('acknowledgedBy', (v) => v as String?),
    acknowledgedAt: $checkedConvert(
      'acknowledgedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    egress: $checkedConvert(
      'egress',
      (v) => $enumDecode(_$MediaProviderEgressEnumEnumMap, v),
    ),
    permissionRecorded: $checkedConvert(
      'permissionRecorded',
      (v) => v as bool?,
    ),
    credentialSet: $checkedConvert('credentialSet', (v) => v as bool?),
    lastImportAt: $checkedConvert(
      'lastImportAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastImportStatus: $checkedConvert(
      'lastImportStatus',
      (v) => $enumDecodeNullable(_$MediaProviderLastImportStatusEnumEnumMap, v),
    ),
    ratingsCount: $checkedConvert('ratingsCount', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$MediaProviderToJson(
  MediaProvider instance,
) => <String, dynamic>{
  'id': _$MediaProviderIdEnumEnumMap[instance.id]!,
  'name': instance.name,
  'enabled': instance.enabled,
  'defaultEnabled': ?instance.defaultEnabled,
  'licence': instance.licence,
  'licenceUrl': ?instance.licenceUrl,
  'attribution': ?instance.attribution,
  'licenceAcknowledged': instance.licenceAcknowledged,
  'acknowledgedBy': ?instance.acknowledgedBy,
  'acknowledgedAt': ?instance.acknowledgedAt?.toIso8601String(),
  'egress': _$MediaProviderEgressEnumEnumMap[instance.egress]!,
  'permissionRecorded': ?instance.permissionRecorded,
  'credentialSet': ?instance.credentialSet,
  'lastImportAt': ?instance.lastImportAt?.toIso8601String(),
  'lastImportStatus':
      ?_$MediaProviderLastImportStatusEnumEnumMap[instance.lastImportStatus],
  'ratingsCount': ?instance.ratingsCount,
};

const _$MediaProviderIdEnumEnumMap = {
  MediaProviderIdEnum.wikidata: 'wikidata',
  MediaProviderIdEnum.partisanWeb: 'partisan-web',
  MediaProviderIdEnum.allsides: 'allsides',
  MediaProviderIdEnum.mbfc: 'mbfc',
  MediaProviderIdEnum.operator_: 'operator',
};

const _$MediaProviderEgressEnumEnumMap = {
  MediaProviderEgressEnum.OPEN: 'OPEN',
  MediaProviderEgressEnum.LOCAL_ONLY: 'LOCAL_ONLY',
};

const _$MediaProviderLastImportStatusEnumEnumMap = {
  MediaProviderLastImportStatusEnum.ok: 'ok',
  MediaProviderLastImportStatusEnum.partial: 'partial',
  MediaProviderLastImportStatusEnum.failed: 'failed',
};
