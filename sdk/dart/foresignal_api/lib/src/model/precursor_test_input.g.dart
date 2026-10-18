// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestInputCWProxy {
  PrecursorTestInput name(String name);

  PrecursorTestInput statement(String? statement);

  PrecursorTestInput antecedent(PrecursorTestAntecedent antecedent);

  PrecursorTestInput consequent(PrecursorTestAntecedent consequent);

  PrecursorTestInput minLag(String? minLag);

  PrecursorTestInput maxLag(String maxLag);

  PrecursorTestInput spatial(PrecursorTestSpatial spatial);

  PrecursorTestInput dedupWindow(String? dedupWindow);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestInput(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestInput call({
    String name,
    String? statement,
    PrecursorTestAntecedent antecedent,
    PrecursorTestAntecedent consequent,
    String? minLag,
    String maxLag,
    PrecursorTestSpatial spatial,
    String? dedupWindow,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestInput.copyWith(...)` or call `instanceOfPrecursorTestInput.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestInputCWProxyImpl implements _$PrecursorTestInputCWProxy {
  const _$PrecursorTestInputCWProxyImpl(this._value);

  final PrecursorTestInput _value;

  @override
  PrecursorTestInput name(String name) => call(name: name);

  @override
  PrecursorTestInput statement(String? statement) => call(statement: statement);

  @override
  PrecursorTestInput antecedent(PrecursorTestAntecedent antecedent) =>
      call(antecedent: antecedent);

  @override
  PrecursorTestInput consequent(PrecursorTestAntecedent consequent) =>
      call(consequent: consequent);

  @override
  PrecursorTestInput minLag(String? minLag) => call(minLag: minLag);

  @override
  PrecursorTestInput maxLag(String maxLag) => call(maxLag: maxLag);

  @override
  PrecursorTestInput spatial(PrecursorTestSpatial spatial) =>
      call(spatial: spatial);

  @override
  PrecursorTestInput dedupWindow(String? dedupWindow) =>
      call(dedupWindow: dedupWindow);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? statement = const $CopyWithPlaceholder(),
    Object? antecedent = const $CopyWithPlaceholder(),
    Object? consequent = const $CopyWithPlaceholder(),
    Object? minLag = const $CopyWithPlaceholder(),
    Object? maxLag = const $CopyWithPlaceholder(),
    Object? spatial = const $CopyWithPlaceholder(),
    Object? dedupWindow = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      statement: statement == const $CopyWithPlaceholder()
          ? _value.statement
          // ignore: cast_nullable_to_non_nullable
          : statement as String?,
      antecedent:
          antecedent == const $CopyWithPlaceholder() || antecedent == null
          ? _value.antecedent
          // ignore: cast_nullable_to_non_nullable
          : antecedent as PrecursorTestAntecedent,
      consequent:
          consequent == const $CopyWithPlaceholder() || consequent == null
          ? _value.consequent
          // ignore: cast_nullable_to_non_nullable
          : consequent as PrecursorTestAntecedent,
      minLag: minLag == const $CopyWithPlaceholder()
          ? _value.minLag
          // ignore: cast_nullable_to_non_nullable
          : minLag as String?,
      maxLag: maxLag == const $CopyWithPlaceholder() || maxLag == null
          ? _value.maxLag
          // ignore: cast_nullable_to_non_nullable
          : maxLag as String,
      spatial: spatial == const $CopyWithPlaceholder() || spatial == null
          ? _value.spatial
          // ignore: cast_nullable_to_non_nullable
          : spatial as PrecursorTestSpatial,
      dedupWindow: dedupWindow == const $CopyWithPlaceholder()
          ? _value.dedupWindow
          // ignore: cast_nullable_to_non_nullable
          : dedupWindow as String?,
    );
  }
}

extension $PrecursorTestInputCopyWith on PrecursorTestInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestInput.copyWith(...)` or `instanceOfPrecursorTestInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestInputCWProxy get copyWith =>
      _$PrecursorTestInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestInput _$PrecursorTestInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PrecursorTestInput', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'name',
          'antecedent',
          'consequent',
          'maxLag',
          'spatial',
        ],
      );
      final val = PrecursorTestInput(
        name: $checkedConvert('name', (v) => v as String),
        statement: $checkedConvert('statement', (v) => v as String?),
        antecedent: $checkedConvert(
          'antecedent',
          (v) => PrecursorTestAntecedent.fromJson(v as Map<String, dynamic>),
        ),
        consequent: $checkedConvert(
          'consequent',
          (v) => PrecursorTestAntecedent.fromJson(v as Map<String, dynamic>),
        ),
        minLag: $checkedConvert('minLag', (v) => v as String?),
        maxLag: $checkedConvert('maxLag', (v) => v as String),
        spatial: $checkedConvert(
          'spatial',
          (v) => PrecursorTestSpatial.fromJson(v as Map<String, dynamic>),
        ),
        dedupWindow: $checkedConvert('dedupWindow', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PrecursorTestInputToJson(PrecursorTestInput instance) =>
    <String, dynamic>{
      'name': instance.name,
      'statement': ?instance.statement,
      'antecedent': instance.antecedent.toJson(),
      'consequent': instance.consequent.toJson(),
      'minLag': ?instance.minLag,
      'maxLag': instance.maxLag,
      'spatial': instance.spatial.toJson(),
      'dedupWindow': ?instance.dedupWindow,
    };
