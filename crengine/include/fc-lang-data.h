// FontConfig database of language orthographies.
// License: Public Domain.
// This file is autogenerated from fc-lang database.
// https://www.freedesktop.org/wiki/Software/fontconfig/
// https://gitlab.freedesktop.org/fontconfig/fontconfig/tree/master/fc-lang
// by convert utility from https://github.com/virxkane/freetype_textdraw

#ifndef FC_LANG_DATA_H
#define FC_LANG_DATA_H

#ifdef __cplusplus
extern "C" {
#endif

#define FC_LANG_DATA_SZ  248

struct fc_lang_rec
{
	const char* lang_code;
	const unsigned int char_set_sz;
	const unsigned int* char_set;
};

/**
 * @brief Return pointer to FontConfig database of language orthographies
 * @return array of fc_lang_rec records.
 */
const struct fc_lang_rec* get_fc_lang_data();

/**
 * @brief Get count of records in the FontConfig database of language orthographies.
 * @return Count of records in array.
 */
unsigned int get_fc_lang_data_size();

/**
 * @brief Find language in database by code
 * @param lang_code language code is exactly as it appears in the fc_lang catalog.
 * @return Pointer to fc_lang_rec instance if language found, NULL otherwise.
 */
const struct fc_lang_rec* fc_lang_find(const char* lang_code);

#ifdef __cplusplus
}
#endif

#endif // FC_LANG_DATA_H