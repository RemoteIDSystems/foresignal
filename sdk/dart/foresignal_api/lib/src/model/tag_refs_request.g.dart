// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tag_refs_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TagRefsRequestCWProxy {
  TagRefsRequest refs(List<String> refs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TagRefsRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TagRefsRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  TagRefsRequest call({List<String> refs});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTagRefsRequest.copyWith(...)` or call `instanceOfTagRefsRequest.copyWith.fieldName(value)` for a single field.
class _$TagRefsRequestCWProxyImpl implements _$TagRefsRequestCWProxy {
  const _$TagRefsRequestCWProxyImpl(this._value);

  final TagRefsRequest _value;

  @override
  TagRefsRequest refs(List<String> refs) => call(refs: refs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TagRefsRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TagRefsRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TagRefsRequest call({Object? refs = const $CopyWithPlaceholder()}) {
    return TagRefsRequest(
      refs: refs == const $CopyWithPlaceholder() || refs == null
          ? _value.refs
          // ignore: cast_nullable_to_non_nullable
          : refs as List<String>,
    );
  }
}

extension $TagRefsRequestCopyWith on TagRefsRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTagRefsRequest.copyWith(...)` or `instanceOfTagRefsRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TagRefsRequestCWProxy get copyWith => _$TagRefsRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TagRefsRequest _$TagRefsRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TagRefsRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['refs']);
      final val = TagRefsRequest(
        refs: $checkedConvert(
          'refs',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TagRefsRequestToJson(TagRefsRequest instance) =>
    <String, dynamic>{'refs': instance.refs};
