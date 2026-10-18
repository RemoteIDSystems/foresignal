// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'spc_outlook_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SpcOutlookPayloadCWProxy {
  SpcOutlookPayload day(int day);

  SpcOutlookPayload type(SpcOutlookPayloadTypeEnum type);

  SpcOutlookPayload label(String label);

  SpcOutlookPayload label2(String? label2);

  SpcOutlookPayload dn(int dn);

  SpcOutlookPayload issue(DateTime? issue);

  SpcOutlookPayload valid(DateTime? valid);

  SpcOutlookPayload expire(DateTime? expire);

  SpcOutlookPayload title(String title);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SpcOutlookPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SpcOutlookPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  SpcOutlookPayload call({
    int day,
    SpcOutlookPayloadTypeEnum type,
    String label,
    String? label2,
    int dn,
    DateTime? issue,
    DateTime? valid,
    DateTime? expire,
    String title,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSpcOutlookPayload.copyWith(...)` or call `instanceOfSpcOutlookPayload.copyWith.fieldName(value)` for a single field.
class _$SpcOutlookPayloadCWProxyImpl implements _$SpcOutlookPayloadCWProxy {
  const _$SpcOutlookPayloadCWProxyImpl(this._value);

  final SpcOutlookPayload _value;

  @override
  SpcOutlookPayload day(int day) => call(day: day);

  @override
  SpcOutlookPayload type(SpcOutlookPayloadTypeEnum type) => call(type: type);

  @override
  SpcOutlookPayload label(String label) => call(label: label);

  @override
  SpcOutlookPayload label2(String? label2) => call(label2: label2);

  @override
  SpcOutlookPayload dn(int dn) => call(dn: dn);

  @override
  SpcOutlookPayload issue(DateTime? issue) => call(issue: issue);

  @override
  SpcOutlookPayload valid(DateTime? valid) => call(valid: valid);

  @override
  SpcOutlookPayload expire(DateTime? expire) => call(expire: expire);

  @override
  SpcOutlookPayload title(String title) => call(title: title);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SpcOutlookPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SpcOutlookPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SpcOutlookPayload call({
    Object? day = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? label2 = const $CopyWithPlaceholder(),
    Object? dn = const $CopyWithPlaceholder(),
    Object? issue = const $CopyWithPlaceholder(),
    Object? valid = const $CopyWithPlaceholder(),
    Object? expire = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
  }) {
    return SpcOutlookPayload(
      day: day == const $CopyWithPlaceholder() || day == null
          ? _value.day
          // ignore: cast_nullable_to_non_nullable
          : day as int,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as SpcOutlookPayloadTypeEnum,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String,
      label2: label2 == const $CopyWithPlaceholder()
          ? _value.label2
          // ignore: cast_nullable_to_non_nullable
          : label2 as String?,
      dn: dn == const $CopyWithPlaceholder() || dn == null
          ? _value.dn
          // ignore: cast_nullable_to_non_nullable
          : dn as int,
      issue: issue == const $CopyWithPlaceholder()
          ? _value.issue
          // ignore: cast_nullable_to_non_nullable
          : issue as DateTime?,
      valid: valid == const $CopyWithPlaceholder()
          ? _value.valid
          // ignore: cast_nullable_to_non_nullable
          : valid as DateTime?,
      expire: expire == const $CopyWithPlaceholder()
          ? _value.expire
          // ignore: cast_nullable_to_non_nullable
          : expire as DateTime?,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
    );
  }
}

extension $SpcOutlookPayloadCopyWith on SpcOutlookPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSpcOutlookPayload.copyWith(...)` or `instanceOfSpcOutlookPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SpcOutlookPayloadCWProxy get copyWith =>
      _$SpcOutlookPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SpcOutlookPayload _$SpcOutlookPayloadFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SpcOutlookPayload', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['day', 'type', 'label', 'dn', 'title'],
      );
      final val = SpcOutlookPayload(
        day: $checkedConvert('day', (v) => (v as num).toInt()),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$SpcOutlookPayloadTypeEnumEnumMap, v),
        ),
        label: $checkedConvert('label', (v) => v as String),
        label2: $checkedConvert('label2', (v) => v as String?),
        dn: $checkedConvert('dn', (v) => (v as num).toInt()),
        issue: $checkedConvert(
          'issue',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        valid: $checkedConvert(
          'valid',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        expire: $checkedConvert(
          'expire',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        title: $checkedConvert('title', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$SpcOutlookPayloadToJson(SpcOutlookPayload instance) =>
    <String, dynamic>{
      'day': instance.day,
      'type': _$SpcOutlookPayloadTypeEnumEnumMap[instance.type]!,
      'label': instance.label,
      'label2': ?instance.label2,
      'dn': instance.dn,
      'issue': ?instance.issue?.toIso8601String(),
      'valid': ?instance.valid?.toIso8601String(),
      'expire': ?instance.expire?.toIso8601String(),
      'title': instance.title,
    };

const _$SpcOutlookPayloadTypeEnumEnumMap = {
  SpcOutlookPayloadTypeEnum.cat: 'cat',
  SpcOutlookPayloadTypeEnum.torn: 'torn',
  SpcOutlookPayloadTypeEnum.wind: 'wind',
  SpcOutlookPayloadTypeEnum.hail: 'hail',
  SpcOutlookPayloadTypeEnum.sigtorn: 'sigtorn',
  SpcOutlookPayloadTypeEnum.sigwind: 'sigwind',
  SpcOutlookPayloadTypeEnum.sighail: 'sighail',
  SpcOutlookPayloadTypeEnum.prob: 'prob',
  SpcOutlookPayloadTypeEnum.sigprob: 'sigprob',
};
