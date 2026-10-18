// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_set_draft_warnings_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisSetDraftWarningsInnerCWProxy {
  HypothesisSetDraftWarningsInner code(String code);

  HypothesisSetDraftWarningsInner message(String message);

  HypothesisSetDraftWarningsInner path(String? path);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetDraftWarningsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetDraftWarningsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisSetDraftWarningsInner call({
    String code,
    String message,
    String? path,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisSetDraftWarningsInner.copyWith(...)` or call `instanceOfHypothesisSetDraftWarningsInner.copyWith.fieldName(value)` for a single field.
class _$HypothesisSetDraftWarningsInnerCWProxyImpl
    implements _$HypothesisSetDraftWarningsInnerCWProxy {
  const _$HypothesisSetDraftWarningsInnerCWProxyImpl(this._value);

  final HypothesisSetDraftWarningsInner _value;

  @override
  HypothesisSetDraftWarningsInner code(String code) => call(code: code);

  @override
  HypothesisSetDraftWarningsInner message(String message) =>
      call(message: message);

  @override
  HypothesisSetDraftWarningsInner path(String? path) => call(path: path);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetDraftWarningsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetDraftWarningsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisSetDraftWarningsInner call({
    Object? code = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? path = const $CopyWithPlaceholder(),
  }) {
    return HypothesisSetDraftWarningsInner(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String,
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String,
      path: path == const $CopyWithPlaceholder()
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String?,
    );
  }
}

extension $HypothesisSetDraftWarningsInnerCopyWith
    on HypothesisSetDraftWarningsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisSetDraftWarningsInner.copyWith(...)` or `instanceOfHypothesisSetDraftWarningsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisSetDraftWarningsInnerCWProxy get copyWith =>
      _$HypothesisSetDraftWarningsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisSetDraftWarningsInner _$HypothesisSetDraftWarningsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisSetDraftWarningsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['code', 'message']);
  final val = HypothesisSetDraftWarningsInner(
    code: $checkedConvert('code', (v) => v as String),
    message: $checkedConvert('message', (v) => v as String),
    path: $checkedConvert('path', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$HypothesisSetDraftWarningsInnerToJson(
  HypothesisSetDraftWarningsInner instance,
) => <String, dynamic>{
  'code': instance.code,
  'message': instance.message,
  'path': ?instance.path,
};
