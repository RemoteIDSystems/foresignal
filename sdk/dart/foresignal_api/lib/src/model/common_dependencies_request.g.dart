// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'common_dependencies_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CommonDependenciesRequestCWProxy {
  CommonDependenciesRequest refs(List<String> refs);

  CommonDependenciesRequest asOf(DateTime? asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CommonDependenciesRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CommonDependenciesRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  CommonDependenciesRequest call({List<String> refs, DateTime? asOf});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCommonDependenciesRequest.copyWith(...)` or call `instanceOfCommonDependenciesRequest.copyWith.fieldName(value)` for a single field.
class _$CommonDependenciesRequestCWProxyImpl
    implements _$CommonDependenciesRequestCWProxy {
  const _$CommonDependenciesRequestCWProxyImpl(this._value);

  final CommonDependenciesRequest _value;

  @override
  CommonDependenciesRequest refs(List<String> refs) => call(refs: refs);

  @override
  CommonDependenciesRequest asOf(DateTime? asOf) => call(asOf: asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CommonDependenciesRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CommonDependenciesRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CommonDependenciesRequest call({
    Object? refs = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
  }) {
    return CommonDependenciesRequest(
      refs: refs == const $CopyWithPlaceholder() || refs == null
          ? _value.refs
          // ignore: cast_nullable_to_non_nullable
          : refs as List<String>,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
    );
  }
}

extension $CommonDependenciesRequestCopyWith on CommonDependenciesRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCommonDependenciesRequest.copyWith(...)` or `instanceOfCommonDependenciesRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CommonDependenciesRequestCWProxy get copyWith =>
      _$CommonDependenciesRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CommonDependenciesRequest _$CommonDependenciesRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CommonDependenciesRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['refs']);
  final val = CommonDependenciesRequest(
    refs: $checkedConvert(
      'refs',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$CommonDependenciesRequestToJson(
  CommonDependenciesRequest instance,
) => <String, dynamic>{
  'refs': instance.refs,
  'asOf': ?instance.asOf?.toIso8601String(),
};
