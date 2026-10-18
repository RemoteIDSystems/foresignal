// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'emwin_warning_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EmwinWarningPayloadCWProxy {
  EmwinWarningPayload action(String action);

  EmwinWarningPayload phenomena(String phenomena);

  EmwinWarningPayload significance(String significance);

  EmwinWarningPayload office(String office);

  EmwinWarningPayload etn(int etn);

  EmwinWarningPayload validFrom(DateTime validFrom);

  EmwinWarningPayload validUntil(DateTime validUntil);

  EmwinWarningPayload headline(String? headline);

  EmwinWarningPayload fallback(bool fallback);

  EmwinWarningPayload authoritativeSource(
    EmwinWarningPayloadAuthoritativeSourceEnum authoritativeSource,
  );

  EmwinWarningPayload duplicateOf(EmwinWarningPayloadDuplicateOf? duplicateOf);

  EmwinWarningPayload receivedVia(
    EmwinWarningPayloadReceivedViaEnum? receivedVia,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EmwinWarningPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EmwinWarningPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  EmwinWarningPayload call({
    String action,
    String phenomena,
    String significance,
    String office,
    int etn,
    DateTime validFrom,
    DateTime validUntil,
    String? headline,
    bool fallback,
    EmwinWarningPayloadAuthoritativeSourceEnum authoritativeSource,
    EmwinWarningPayloadDuplicateOf? duplicateOf,
    EmwinWarningPayloadReceivedViaEnum? receivedVia,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEmwinWarningPayload.copyWith(...)` or call `instanceOfEmwinWarningPayload.copyWith.fieldName(value)` for a single field.
class _$EmwinWarningPayloadCWProxyImpl implements _$EmwinWarningPayloadCWProxy {
  const _$EmwinWarningPayloadCWProxyImpl(this._value);

  final EmwinWarningPayload _value;

  @override
  EmwinWarningPayload action(String action) => call(action: action);

  @override
  EmwinWarningPayload phenomena(String phenomena) => call(phenomena: phenomena);

  @override
  EmwinWarningPayload significance(String significance) =>
      call(significance: significance);

  @override
  EmwinWarningPayload office(String office) => call(office: office);

  @override
  EmwinWarningPayload etn(int etn) => call(etn: etn);

  @override
  EmwinWarningPayload validFrom(DateTime validFrom) =>
      call(validFrom: validFrom);

  @override
  EmwinWarningPayload validUntil(DateTime validUntil) =>
      call(validUntil: validUntil);

  @override
  EmwinWarningPayload headline(String? headline) => call(headline: headline);

  @override
  EmwinWarningPayload fallback(bool fallback) => call(fallback: fallback);

  @override
  EmwinWarningPayload authoritativeSource(
    EmwinWarningPayloadAuthoritativeSourceEnum authoritativeSource,
  ) => call(authoritativeSource: authoritativeSource);

  @override
  EmwinWarningPayload duplicateOf(
    EmwinWarningPayloadDuplicateOf? duplicateOf,
  ) => call(duplicateOf: duplicateOf);

  @override
  EmwinWarningPayload receivedVia(
    EmwinWarningPayloadReceivedViaEnum? receivedVia,
  ) => call(receivedVia: receivedVia);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EmwinWarningPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EmwinWarningPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EmwinWarningPayload call({
    Object? action = const $CopyWithPlaceholder(),
    Object? phenomena = const $CopyWithPlaceholder(),
    Object? significance = const $CopyWithPlaceholder(),
    Object? office = const $CopyWithPlaceholder(),
    Object? etn = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? validUntil = const $CopyWithPlaceholder(),
    Object? headline = const $CopyWithPlaceholder(),
    Object? fallback = const $CopyWithPlaceholder(),
    Object? authoritativeSource = const $CopyWithPlaceholder(),
    Object? duplicateOf = const $CopyWithPlaceholder(),
    Object? receivedVia = const $CopyWithPlaceholder(),
  }) {
    return EmwinWarningPayload(
      action: action == const $CopyWithPlaceholder() || action == null
          ? _value.action
          // ignore: cast_nullable_to_non_nullable
          : action as String,
      phenomena: phenomena == const $CopyWithPlaceholder() || phenomena == null
          ? _value.phenomena
          // ignore: cast_nullable_to_non_nullable
          : phenomena as String,
      significance:
          significance == const $CopyWithPlaceholder() || significance == null
          ? _value.significance
          // ignore: cast_nullable_to_non_nullable
          : significance as String,
      office: office == const $CopyWithPlaceholder() || office == null
          ? _value.office
          // ignore: cast_nullable_to_non_nullable
          : office as String,
      etn: etn == const $CopyWithPlaceholder() || etn == null
          ? _value.etn
          // ignore: cast_nullable_to_non_nullable
          : etn as int,
      validFrom: validFrom == const $CopyWithPlaceholder() || validFrom == null
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime,
      validUntil:
          validUntil == const $CopyWithPlaceholder() || validUntil == null
          ? _value.validUntil
          // ignore: cast_nullable_to_non_nullable
          : validUntil as DateTime,
      headline: headline == const $CopyWithPlaceholder()
          ? _value.headline
          // ignore: cast_nullable_to_non_nullable
          : headline as String?,
      fallback: fallback == const $CopyWithPlaceholder() || fallback == null
          ? _value.fallback
          // ignore: cast_nullable_to_non_nullable
          : fallback as bool,
      authoritativeSource:
          authoritativeSource == const $CopyWithPlaceholder() ||
              authoritativeSource == null
          ? _value.authoritativeSource
          // ignore: cast_nullable_to_non_nullable
          : authoritativeSource as EmwinWarningPayloadAuthoritativeSourceEnum,
      duplicateOf: duplicateOf == const $CopyWithPlaceholder()
          ? _value.duplicateOf
          // ignore: cast_nullable_to_non_nullable
          : duplicateOf as EmwinWarningPayloadDuplicateOf?,
      receivedVia: receivedVia == const $CopyWithPlaceholder()
          ? _value.receivedVia
          // ignore: cast_nullable_to_non_nullable
          : receivedVia as EmwinWarningPayloadReceivedViaEnum?,
    );
  }
}

extension $EmwinWarningPayloadCopyWith on EmwinWarningPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEmwinWarningPayload.copyWith(...)` or `instanceOfEmwinWarningPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EmwinWarningPayloadCWProxy get copyWith =>
      _$EmwinWarningPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EmwinWarningPayload _$EmwinWarningPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EmwinWarningPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'action',
      'phenomena',
      'significance',
      'office',
      'etn',
      'validFrom',
      'validUntil',
      'fallback',
      'authoritativeSource',
    ],
  );
  final val = EmwinWarningPayload(
    action: $checkedConvert('action', (v) => v as String),
    phenomena: $checkedConvert('phenomena', (v) => v as String),
    significance: $checkedConvert('significance', (v) => v as String),
    office: $checkedConvert('office', (v) => v as String),
    etn: $checkedConvert('etn', (v) => (v as num).toInt()),
    validFrom: $checkedConvert('validFrom', (v) => DateTime.parse(v as String)),
    validUntil: $checkedConvert(
      'validUntil',
      (v) => DateTime.parse(v as String),
    ),
    headline: $checkedConvert('headline', (v) => v as String?),
    fallback: $checkedConvert('fallback', (v) => v as bool),
    authoritativeSource: $checkedConvert(
      'authoritativeSource',
      (v) =>
          $enumDecode(_$EmwinWarningPayloadAuthoritativeSourceEnumEnumMap, v),
    ),
    duplicateOf: $checkedConvert(
      'duplicateOf',
      (v) => v == null
          ? null
          : EmwinWarningPayloadDuplicateOf.fromJson(v as Map<String, dynamic>),
    ),
    receivedVia: $checkedConvert(
      'receivedVia',
      (v) =>
          $enumDecodeNullable(_$EmwinWarningPayloadReceivedViaEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$EmwinWarningPayloadToJson(
  EmwinWarningPayload instance,
) => <String, dynamic>{
  'action': instance.action,
  'phenomena': instance.phenomena,
  'significance': instance.significance,
  'office': instance.office,
  'etn': instance.etn,
  'validFrom': instance.validFrom.toIso8601String(),
  'validUntil': instance.validUntil.toIso8601String(),
  'headline': ?instance.headline,
  'fallback': instance.fallback,
  'authoritativeSource':
      _$EmwinWarningPayloadAuthoritativeSourceEnumEnumMap[instance
          .authoritativeSource]!,
  'duplicateOf': ?instance.duplicateOf?.toJson(),
  'receivedVia':
      ?_$EmwinWarningPayloadReceivedViaEnumEnumMap[instance.receivedVia],
};

const _$EmwinWarningPayloadAuthoritativeSourceEnumEnumMap = {
  EmwinWarningPayloadAuthoritativeSourceEnum.nwsAlerts: 'nws-alerts',
};

const _$EmwinWarningPayloadReceivedViaEnumEnumMap = {
  EmwinWarningPayloadReceivedViaEnum.receivedViaGOESHRITEMWIN:
      'Received via GOES HRIT EMWIN',
};
