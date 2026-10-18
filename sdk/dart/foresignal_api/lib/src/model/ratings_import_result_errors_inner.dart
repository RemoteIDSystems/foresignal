//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ratings_import_result_errors_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RatingsImportResultErrorsInner {
  /// Returns a new [RatingsImportResultErrorsInner] instance.
  RatingsImportResultErrorsInner({

     this.row,

    required  this.message,
  });

  @JsonKey(
    
    name: r'row',
    required: false,
    includeIfNull: false,
  )


  final int? row;



  @JsonKey(
    
    name: r'message',
    required: true,
    includeIfNull: false,
  )


  final String message;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RatingsImportResultErrorsInner &&
      other.row == row &&
      other.message == message;

    @override
    int get hashCode =>
        (row == null ? 0 : row.hashCode) +
        message.hashCode;

  factory RatingsImportResultErrorsInner.fromJson(Map<String, dynamic> json) => _$RatingsImportResultErrorsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$RatingsImportResultErrorsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

