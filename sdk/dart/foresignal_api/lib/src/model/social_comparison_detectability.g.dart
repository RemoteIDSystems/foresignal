// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_comparison_detectability.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialComparisonDetectabilityCWProxy {
  SocialComparisonDetectability p(num p);

  SocialComparisonDetectability lo(num lo);

  SocialComparisonDetectability hi(num hi);

  SocialComparisonDetectability methods(List<String> methods);

  SocialComparisonDetectability ok(bool ok);

  SocialComparisonDetectability reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialComparisonDetectability(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialComparisonDetectability(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialComparisonDetectability call({
    num p,
    num lo,
    num hi,
    List<String> methods,
    bool ok,
    String? reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialComparisonDetectability.copyWith(...)` or call `instanceOfSocialComparisonDetectability.copyWith.fieldName(value)` for a single field.
class _$SocialComparisonDetectabilityCWProxyImpl
    implements _$SocialComparisonDetectabilityCWProxy {
  const _$SocialComparisonDetectabilityCWProxyImpl(this._value);

  final SocialComparisonDetectability _value;

  @override
  SocialComparisonDetectability p(num p) => call(p: p);

  @override
  SocialComparisonDetectability lo(num lo) => call(lo: lo);

  @override
  SocialComparisonDetectability hi(num hi) => call(hi: hi);

  @override
  SocialComparisonDetectability methods(List<String> methods) =>
      call(methods: methods);

  @override
  SocialComparisonDetectability ok(bool ok) => call(ok: ok);

  @override
  SocialComparisonDetectability reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialComparisonDetectability(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialComparisonDetectability(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialComparisonDetectability call({
    Object? p = const $CopyWithPlaceholder(),
    Object? lo = const $CopyWithPlaceholder(),
    Object? hi = const $CopyWithPlaceholder(),
    Object? methods = const $CopyWithPlaceholder(),
    Object? ok = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return SocialComparisonDetectability(
      p: p == const $CopyWithPlaceholder() || p == null
          ? _value.p
          // ignore: cast_nullable_to_non_nullable
          : p as num,
      lo: lo == const $CopyWithPlaceholder() || lo == null
          ? _value.lo
          // ignore: cast_nullable_to_non_nullable
          : lo as num,
      hi: hi == const $CopyWithPlaceholder() || hi == null
          ? _value.hi
          // ignore: cast_nullable_to_non_nullable
          : hi as num,
      methods: methods == const $CopyWithPlaceholder() || methods == null
          ? _value.methods
          // ignore: cast_nullable_to_non_nullable
          : methods as List<String>,
      ok: ok == const $CopyWithPlaceholder() || ok == null
          ? _value.ok
          // ignore: cast_nullable_to_non_nullable
          : ok as bool,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $SocialComparisonDetectabilityCopyWith
    on SocialComparisonDetectability {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialComparisonDetectability.copyWith(...)` or `instanceOfSocialComparisonDetectability.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialComparisonDetectabilityCWProxy get copyWith =>
      _$SocialComparisonDetectabilityCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialComparisonDetectability _$SocialComparisonDetectabilityFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialComparisonDetectability', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['p', 'lo', 'hi', 'methods', 'ok']);
  final val = SocialComparisonDetectability(
    p: $checkedConvert('p', (v) => v as num),
    lo: $checkedConvert('lo', (v) => v as num),
    hi: $checkedConvert('hi', (v) => v as num),
    methods: $checkedConvert(
      'methods',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    ok: $checkedConvert('ok', (v) => v as bool),
    reason: $checkedConvert('reason', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$SocialComparisonDetectabilityToJson(
  SocialComparisonDetectability instance,
) => <String, dynamic>{
  'p': instance.p,
  'lo': instance.lo,
  'hi': instance.hi,
  'methods': instance.methods,
  'ok': instance.ok,
  'reason': ?instance.reason,
};
