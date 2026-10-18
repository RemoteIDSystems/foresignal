// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestCWProxy {
  PrecursorTest id(String id);

  PrecursorTest name(String name);

  PrecursorTest statement(String? statement);

  PrecursorTest antecedent(PrecursorTestAntecedent antecedent);

  PrecursorTest consequent(PrecursorTestAntecedent consequent);

  PrecursorTest minLag(String? minLag);

  PrecursorTest maxLag(String maxLag);

  PrecursorTest spatial(PrecursorTestSpatial spatial);

  PrecursorTest dedupWindow(String? dedupWindow);

  PrecursorTest createdBy(String? createdBy);

  PrecursorTest createdAt(DateTime createdAt);

  PrecursorTest updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTest(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTest call({
    String id,
    String name,
    String? statement,
    PrecursorTestAntecedent antecedent,
    PrecursorTestAntecedent consequent,
    String? minLag,
    String maxLag,
    PrecursorTestSpatial spatial,
    String? dedupWindow,
    String? createdBy,
    DateTime createdAt,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTest.copyWith(...)` or call `instanceOfPrecursorTest.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestCWProxyImpl implements _$PrecursorTestCWProxy {
  const _$PrecursorTestCWProxyImpl(this._value);

  final PrecursorTest _value;

  @override
  PrecursorTest id(String id) => call(id: id);

  @override
  PrecursorTest name(String name) => call(name: name);

  @override
  PrecursorTest statement(String? statement) => call(statement: statement);

  @override
  PrecursorTest antecedent(PrecursorTestAntecedent antecedent) =>
      call(antecedent: antecedent);

  @override
  PrecursorTest consequent(PrecursorTestAntecedent consequent) =>
      call(consequent: consequent);

  @override
  PrecursorTest minLag(String? minLag) => call(minLag: minLag);

  @override
  PrecursorTest maxLag(String maxLag) => call(maxLag: maxLag);

  @override
  PrecursorTest spatial(PrecursorTestSpatial spatial) => call(spatial: spatial);

  @override
  PrecursorTest dedupWindow(String? dedupWindow) =>
      call(dedupWindow: dedupWindow);

  @override
  PrecursorTest createdBy(String? createdBy) => call(createdBy: createdBy);

  @override
  PrecursorTest createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  PrecursorTest updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTest call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? statement = const $CopyWithPlaceholder(),
    Object? antecedent = const $CopyWithPlaceholder(),
    Object? consequent = const $CopyWithPlaceholder(),
    Object? minLag = const $CopyWithPlaceholder(),
    Object? maxLag = const $CopyWithPlaceholder(),
    Object? spatial = const $CopyWithPlaceholder(),
    Object? dedupWindow = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTest(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
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
      createdBy: createdBy == const $CopyWithPlaceholder()
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $PrecursorTestCopyWith on PrecursorTest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTest.copyWith(...)` or `instanceOfPrecursorTest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestCWProxy get copyWith => _$PrecursorTestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTest _$PrecursorTestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PrecursorTest', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'name',
          'antecedent',
          'consequent',
          'maxLag',
          'spatial',
          'createdAt',
        ],
      );
      final val = PrecursorTest(
        id: $checkedConvert('id', (v) => v as String),
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
        createdBy: $checkedConvert('createdBy', (v) => v as String?),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => DateTime.parse(v as String),
        ),
        updatedAt: $checkedConvert(
          'updatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PrecursorTestToJson(PrecursorTest instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'statement': ?instance.statement,
      'antecedent': instance.antecedent.toJson(),
      'consequent': instance.consequent.toJson(),
      'minLag': ?instance.minLag,
      'maxLag': instance.maxLag,
      'spatial': instance.spatial.toJson(),
      'dedupWindow': ?instance.dedupWindow,
      'createdBy': ?instance.createdBy,
      'createdAt': instance.createdAt.toIso8601String(),
      'updatedAt': ?instance.updatedAt?.toIso8601String(),
    };
