// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'annotation_reply_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnnotationReplyInputCWProxy {
  AnnotationReplyInput body(String body);

  AnnotationReplyInput private(bool? private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnnotationReplyInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnnotationReplyInput(...).copyWith(id: 12, name: "My name")
  /// ```
  AnnotationReplyInput call({String body, bool? private});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnnotationReplyInput.copyWith(...)` or call `instanceOfAnnotationReplyInput.copyWith.fieldName(value)` for a single field.
class _$AnnotationReplyInputCWProxyImpl
    implements _$AnnotationReplyInputCWProxy {
  const _$AnnotationReplyInputCWProxyImpl(this._value);

  final AnnotationReplyInput _value;

  @override
  AnnotationReplyInput body(String body) => call(body: body);

  @override
  AnnotationReplyInput private(bool? private) => call(private: private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnnotationReplyInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnnotationReplyInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnnotationReplyInput call({
    Object? body = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
  }) {
    return AnnotationReplyInput(
      body: body == const $CopyWithPlaceholder() || body == null
          ? _value.body
          // ignore: cast_nullable_to_non_nullable
          : body as String,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
    );
  }
}

extension $AnnotationReplyInputCopyWith on AnnotationReplyInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnnotationReplyInput.copyWith(...)` or `instanceOfAnnotationReplyInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnnotationReplyInputCWProxy get copyWith =>
      _$AnnotationReplyInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnnotationReplyInput _$AnnotationReplyInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnnotationReplyInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['body']);
  final val = AnnotationReplyInput(
    body: $checkedConvert('body', (v) => v as String),
    private: $checkedConvert('private', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$AnnotationReplyInputToJson(
  AnnotationReplyInput instance,
) => <String, dynamic>{'body': instance.body, 'private': ?instance.private};
