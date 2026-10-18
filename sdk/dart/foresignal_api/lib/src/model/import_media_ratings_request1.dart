//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/import_media_ratings_request1_rows_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'import_media_ratings_request1.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ImportMediaRatingsRequest1 {
  /// Returns a new [ImportMediaRatingsRequest1] instance.
  ImportMediaRatingsRequest1({

    required  this.rows,
  });

  @JsonKey(
    
    name: r'rows',
    required: true,
    includeIfNull: false,
  )


  final List<ImportMediaRatingsRequest1RowsInner> rows;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ImportMediaRatingsRequest1 &&
      other.rows == rows;

    @override
    int get hashCode =>
        rows.hashCode;

  factory ImportMediaRatingsRequest1.fromJson(Map<String, dynamic> json) => _$ImportMediaRatingsRequest1FromJson(json);

  Map<String, dynamic> toJson() => _$ImportMediaRatingsRequest1ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

