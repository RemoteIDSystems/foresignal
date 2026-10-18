// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'emwin_warning_payload_duplicate_of.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EmwinWarningPayloadDuplicateOfCWProxy {
  EmwinWarningPayloadDuplicateOf source_(
    EmwinWarningPayloadDuplicateOfSource_Enum source_,
  );

  EmwinWarningPayloadDuplicateOf externalId(String externalId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EmwinWarningPayloadDuplicateOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EmwinWarningPayloadDuplicateOf(...).copyWith(id: 12, name: "My name")
  /// ```
  EmwinWarningPayloadDuplicateOf call({
    EmwinWarningPayloadDuplicateOfSource_Enum source_,
    String externalId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEmwinWarningPayloadDuplicateOf.copyWith(...)` or call `instanceOfEmwinWarningPayloadDuplicateOf.copyWith.fieldName(value)` for a single field.
class _$EmwinWarningPayloadDuplicateOfCWProxyImpl
    implements _$EmwinWarningPayloadDuplicateOfCWProxy {
  const _$EmwinWarningPayloadDuplicateOfCWProxyImpl(this._value);

  final EmwinWarningPayloadDuplicateOf _value;

  @override
  EmwinWarningPayloadDuplicateOf source_(
    EmwinWarningPayloadDuplicateOfSource_Enum source_,
  ) => call(source_: source_);

  @override
  EmwinWarningPayloadDuplicateOf externalId(String externalId) =>
      call(externalId: externalId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EmwinWarningPayloadDuplicateOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EmwinWarningPayloadDuplicateOf(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EmwinWarningPayloadDuplicateOf call({
    Object? source_ = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
  }) {
    return EmwinWarningPayloadDuplicateOf(
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as EmwinWarningPayloadDuplicateOfSource_Enum,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
    );
  }
}

extension $EmwinWarningPayloadDuplicateOfCopyWith
    on EmwinWarningPayloadDuplicateOf {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEmwinWarningPayloadDuplicateOf.copyWith(...)` or `instanceOfEmwinWarningPayloadDuplicateOf.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EmwinWarningPayloadDuplicateOfCWProxy get copyWith =>
      _$EmwinWarningPayloadDuplicateOfCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EmwinWarningPayloadDuplicateOf _$EmwinWarningPayloadDuplicateOfFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EmwinWarningPayloadDuplicateOf', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['source', 'externalId']);
  final val = EmwinWarningPayloadDuplicateOf(
    source_: $checkedConvert(
      'source',
      (v) => $enumDecode(_$EmwinWarningPayloadDuplicateOfSource_EnumEnumMap, v),
    ),
    externalId: $checkedConvert('externalId', (v) => v as String),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$EmwinWarningPayloadDuplicateOfToJson(
  EmwinWarningPayloadDuplicateOf instance,
) => <String, dynamic>{
  'source':
      _$EmwinWarningPayloadDuplicateOfSource_EnumEnumMap[instance.source_]!,
  'externalId': instance.externalId,
};

const _$EmwinWarningPayloadDuplicateOfSource_EnumEnumMap = {
  EmwinWarningPayloadDuplicateOfSource_Enum.nwsAlerts: 'nws-alerts',
};
