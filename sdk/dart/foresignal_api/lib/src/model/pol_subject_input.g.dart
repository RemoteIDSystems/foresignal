// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pol_subject_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PolSubjectInputCWProxy {
  PolSubjectInput subjectType(PolSubjectInputSubjectTypeEnum subjectType);

  PolSubjectInput ref(String ref);

  PolSubjectInput enabled(bool? enabled);

  PolSubjectInput params(PolSubjectInputParams? params);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolSubjectInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolSubjectInput(...).copyWith(id: 12, name: "My name")
  /// ```
  PolSubjectInput call({
    PolSubjectInputSubjectTypeEnum subjectType,
    String ref,
    bool? enabled,
    PolSubjectInputParams? params,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPolSubjectInput.copyWith(...)` or call `instanceOfPolSubjectInput.copyWith.fieldName(value)` for a single field.
class _$PolSubjectInputCWProxyImpl implements _$PolSubjectInputCWProxy {
  const _$PolSubjectInputCWProxyImpl(this._value);

  final PolSubjectInput _value;

  @override
  PolSubjectInput subjectType(PolSubjectInputSubjectTypeEnum subjectType) =>
      call(subjectType: subjectType);

  @override
  PolSubjectInput ref(String ref) => call(ref: ref);

  @override
  PolSubjectInput enabled(bool? enabled) => call(enabled: enabled);

  @override
  PolSubjectInput params(PolSubjectInputParams? params) => call(params: params);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolSubjectInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolSubjectInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PolSubjectInput call({
    Object? subjectType = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
  }) {
    return PolSubjectInput(
      subjectType:
          subjectType == const $CopyWithPlaceholder() || subjectType == null
          ? _value.subjectType
          // ignore: cast_nullable_to_non_nullable
          : subjectType as PolSubjectInputSubjectTypeEnum,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      params: params == const $CopyWithPlaceholder()
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as PolSubjectInputParams?,
    );
  }
}

extension $PolSubjectInputCopyWith on PolSubjectInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPolSubjectInput.copyWith(...)` or `instanceOfPolSubjectInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PolSubjectInputCWProxy get copyWith => _$PolSubjectInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PolSubjectInput _$PolSubjectInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PolSubjectInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['subjectType', 'ref']);
      final val = PolSubjectInput(
        subjectType: $checkedConvert(
          'subjectType',
          (v) => $enumDecode(_$PolSubjectInputSubjectTypeEnumEnumMap, v),
        ),
        ref: $checkedConvert('ref', (v) => v as String),
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
        params: $checkedConvert(
          'params',
          (v) => v == null
              ? null
              : PolSubjectInputParams.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PolSubjectInputToJson(
  PolSubjectInput instance,
) => <String, dynamic>{
  'subjectType': _$PolSubjectInputSubjectTypeEnumEnumMap[instance.subjectType]!,
  'ref': instance.ref,
  'enabled': ?instance.enabled,
  'params': ?instance.params?.toJson(),
};

const _$PolSubjectInputSubjectTypeEnumEnumMap = {
  PolSubjectInputSubjectTypeEnum.record: 'record',
  PolSubjectInputSubjectTypeEnum.entity: 'entity',
  PolSubjectInputSubjectTypeEnum.watchlist: 'watchlist',
};
