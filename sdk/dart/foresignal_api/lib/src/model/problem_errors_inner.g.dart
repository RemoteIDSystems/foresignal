// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'problem_errors_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProblemErrorsInnerCWProxy {
  ProblemErrorsInner field(String? field);

  ProblemErrorsInner message(String? message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProblemErrorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProblemErrorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ProblemErrorsInner call({String? field, String? message});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProblemErrorsInner.copyWith(...)` or call `instanceOfProblemErrorsInner.copyWith.fieldName(value)` for a single field.
class _$ProblemErrorsInnerCWProxyImpl implements _$ProblemErrorsInnerCWProxy {
  const _$ProblemErrorsInnerCWProxyImpl(this._value);

  final ProblemErrorsInner _value;

  @override
  ProblemErrorsInner field(String? field) => call(field: field);

  @override
  ProblemErrorsInner message(String? message) => call(message: message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProblemErrorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProblemErrorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProblemErrorsInner call({
    Object? field = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
  }) {
    return ProblemErrorsInner(
      field: field == const $CopyWithPlaceholder()
          ? _value.field
          // ignore: cast_nullable_to_non_nullable
          : field as String?,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
    );
  }
}

extension $ProblemErrorsInnerCopyWith on ProblemErrorsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProblemErrorsInner.copyWith(...)` or `instanceOfProblemErrorsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProblemErrorsInnerCWProxy get copyWith =>
      _$ProblemErrorsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProblemErrorsInner _$ProblemErrorsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProblemErrorsInner', json, ($checkedConvert) {
      final val = ProblemErrorsInner(
        field: $checkedConvert('field', (v) => v as String?),
        message: $checkedConvert('message', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ProblemErrorsInnerToJson(ProblemErrorsInner instance) =>
    <String, dynamic>{'field': ?instance.field, 'message': ?instance.message};
