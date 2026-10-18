// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trip_hazard.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TripHazardCWProxy {
  TripHazard ref(String ref);

  TripHazard sourceId(String sourceId);

  TripHazard externalId(String externalId);

  TripHazard kind(String kind);

  TripHazard title(String? title);

  TripHazard severity(Severity? severity);

  TripHazard section(TripHazardSectionEnum section);

  TripHazard distanceAlongRouteM(num distanceAlongRouteM);

  TripHazard distanceFromRouteM(num? distanceFromRouteM);

  TripHazard validFrom(DateTime? validFrom);

  TripHazard validUntil(DateTime? validUntil);

  TripHazard ahead(bool? ahead);

  TripHazard etaSeconds(num? etaSeconds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TripHazard(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TripHazard(...).copyWith(id: 12, name: "My name")
  /// ```
  TripHazard call({
    String ref,
    String sourceId,
    String externalId,
    String kind,
    String? title,
    Severity? severity,
    TripHazardSectionEnum section,
    num distanceAlongRouteM,
    num? distanceFromRouteM,
    DateTime? validFrom,
    DateTime? validUntil,
    bool? ahead,
    num? etaSeconds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTripHazard.copyWith(...)` or call `instanceOfTripHazard.copyWith.fieldName(value)` for a single field.
class _$TripHazardCWProxyImpl implements _$TripHazardCWProxy {
  const _$TripHazardCWProxyImpl(this._value);

  final TripHazard _value;

  @override
  TripHazard ref(String ref) => call(ref: ref);

  @override
  TripHazard sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  TripHazard externalId(String externalId) => call(externalId: externalId);

  @override
  TripHazard kind(String kind) => call(kind: kind);

  @override
  TripHazard title(String? title) => call(title: title);

  @override
  TripHazard severity(Severity? severity) => call(severity: severity);

  @override
  TripHazard section(TripHazardSectionEnum section) => call(section: section);

  @override
  TripHazard distanceAlongRouteM(num distanceAlongRouteM) =>
      call(distanceAlongRouteM: distanceAlongRouteM);

  @override
  TripHazard distanceFromRouteM(num? distanceFromRouteM) =>
      call(distanceFromRouteM: distanceFromRouteM);

  @override
  TripHazard validFrom(DateTime? validFrom) => call(validFrom: validFrom);

  @override
  TripHazard validUntil(DateTime? validUntil) => call(validUntil: validUntil);

  @override
  TripHazard ahead(bool? ahead) => call(ahead: ahead);

  @override
  TripHazard etaSeconds(num? etaSeconds) => call(etaSeconds: etaSeconds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TripHazard(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TripHazard(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TripHazard call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? section = const $CopyWithPlaceholder(),
    Object? distanceAlongRouteM = const $CopyWithPlaceholder(),
    Object? distanceFromRouteM = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? validUntil = const $CopyWithPlaceholder(),
    Object? ahead = const $CopyWithPlaceholder(),
    Object? etaSeconds = const $CopyWithPlaceholder(),
  }) {
    return TripHazard(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as Severity?,
      section: section == const $CopyWithPlaceholder() || section == null
          ? _value.section
          // ignore: cast_nullable_to_non_nullable
          : section as TripHazardSectionEnum,
      distanceAlongRouteM:
          distanceAlongRouteM == const $CopyWithPlaceholder() ||
              distanceAlongRouteM == null
          ? _value.distanceAlongRouteM
          // ignore: cast_nullable_to_non_nullable
          : distanceAlongRouteM as num,
      distanceFromRouteM: distanceFromRouteM == const $CopyWithPlaceholder()
          ? _value.distanceFromRouteM
          // ignore: cast_nullable_to_non_nullable
          : distanceFromRouteM as num?,
      validFrom: validFrom == const $CopyWithPlaceholder()
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime?,
      validUntil: validUntil == const $CopyWithPlaceholder()
          ? _value.validUntil
          // ignore: cast_nullable_to_non_nullable
          : validUntil as DateTime?,
      ahead: ahead == const $CopyWithPlaceholder()
          ? _value.ahead
          // ignore: cast_nullable_to_non_nullable
          : ahead as bool?,
      etaSeconds: etaSeconds == const $CopyWithPlaceholder()
          ? _value.etaSeconds
          // ignore: cast_nullable_to_non_nullable
          : etaSeconds as num?,
    );
  }
}

extension $TripHazardCopyWith on TripHazard {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTripHazard.copyWith(...)` or `instanceOfTripHazard.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TripHazardCWProxy get copyWith => _$TripHazardCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TripHazard _$TripHazardFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TripHazard', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'ref',
          'sourceId',
          'externalId',
          'kind',
          'section',
          'distanceAlongRouteM',
        ],
      );
      final val = TripHazard(
        ref: $checkedConvert('ref', (v) => v as String),
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        externalId: $checkedConvert('externalId', (v) => v as String),
        kind: $checkedConvert('kind', (v) => v as String),
        title: $checkedConvert('title', (v) => v as String?),
        severity: $checkedConvert(
          'severity',
          (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
        ),
        section: $checkedConvert(
          'section',
          (v) => $enumDecode(_$TripHazardSectionEnumEnumMap, v),
        ),
        distanceAlongRouteM: $checkedConvert(
          'distanceAlongRouteM',
          (v) => v as num,
        ),
        distanceFromRouteM: $checkedConvert(
          'distanceFromRouteM',
          (v) => v as num?,
        ),
        validFrom: $checkedConvert(
          'validFrom',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        validUntil: $checkedConvert(
          'validUntil',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        ahead: $checkedConvert('ahead', (v) => v as bool?),
        etaSeconds: $checkedConvert('etaSeconds', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$TripHazardToJson(TripHazard instance) =>
    <String, dynamic>{
      'ref': instance.ref,
      'sourceId': instance.sourceId,
      'externalId': instance.externalId,
      'kind': instance.kind,
      'title': ?instance.title,
      'severity': ?_$SeverityEnumMap[instance.severity],
      'section': _$TripHazardSectionEnumEnumMap[instance.section]!,
      'distanceAlongRouteM': instance.distanceAlongRouteM,
      'distanceFromRouteM': ?instance.distanceFromRouteM,
      'validFrom': ?instance.validFrom?.toIso8601String(),
      'validUntil': ?instance.validUntil?.toIso8601String(),
      'ahead': ?instance.ahead,
      'etaSeconds': ?instance.etaSeconds,
    };

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};

const _$TripHazardSectionEnumEnumMap = {
  TripHazardSectionEnum.onRoute: 'on-route',
  TripHazardSectionEnum.nearRoute: 'near-route',
  TripHazardSectionEnum.timing: 'timing',
};
