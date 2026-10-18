// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'base_rate_licences_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BaseRateLicencesInnerCWProxy {
  BaseRateLicencesInner source_(String source_);

  BaseRateLicencesInner licence(String licence);

  BaseRateLicencesInner egress(BaseRateLicencesInnerEgressEnum? egress);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRateLicencesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRateLicencesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  BaseRateLicencesInner call({
    String source_,
    String licence,
    BaseRateLicencesInnerEgressEnum? egress,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBaseRateLicencesInner.copyWith(...)` or call `instanceOfBaseRateLicencesInner.copyWith.fieldName(value)` for a single field.
class _$BaseRateLicencesInnerCWProxyImpl
    implements _$BaseRateLicencesInnerCWProxy {
  const _$BaseRateLicencesInnerCWProxyImpl(this._value);

  final BaseRateLicencesInner _value;

  @override
  BaseRateLicencesInner source_(String source_) => call(source_: source_);

  @override
  BaseRateLicencesInner licence(String licence) => call(licence: licence);

  @override
  BaseRateLicencesInner egress(BaseRateLicencesInnerEgressEnum? egress) =>
      call(egress: egress);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRateLicencesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRateLicencesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BaseRateLicencesInner call({
    Object? source_ = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? egress = const $CopyWithPlaceholder(),
  }) {
    return BaseRateLicencesInner(
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String,
      licence: licence == const $CopyWithPlaceholder() || licence == null
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String,
      egress: egress == const $CopyWithPlaceholder()
          ? _value.egress
          // ignore: cast_nullable_to_non_nullable
          : egress as BaseRateLicencesInnerEgressEnum?,
    );
  }
}

extension $BaseRateLicencesInnerCopyWith on BaseRateLicencesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBaseRateLicencesInner.copyWith(...)` or `instanceOfBaseRateLicencesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BaseRateLicencesInnerCWProxy get copyWith =>
      _$BaseRateLicencesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BaseRateLicencesInner _$BaseRateLicencesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BaseRateLicencesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['source', 'licence']);
  final val = BaseRateLicencesInner(
    source_: $checkedConvert('source', (v) => v as String),
    licence: $checkedConvert('licence', (v) => v as String),
    egress: $checkedConvert(
      'egress',
      (v) => $enumDecodeNullable(_$BaseRateLicencesInnerEgressEnumEnumMap, v),
    ),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$BaseRateLicencesInnerToJson(
  BaseRateLicencesInner instance,
) => <String, dynamic>{
  'source': instance.source_,
  'licence': instance.licence,
  'egress': ?_$BaseRateLicencesInnerEgressEnumEnumMap[instance.egress],
};

const _$BaseRateLicencesInnerEgressEnumEnumMap = {
  BaseRateLicencesInnerEgressEnum.OPEN: 'OPEN',
  BaseRateLicencesInnerEgressEnum.LOCAL_ONLY: 'LOCAL_ONLY',
};
